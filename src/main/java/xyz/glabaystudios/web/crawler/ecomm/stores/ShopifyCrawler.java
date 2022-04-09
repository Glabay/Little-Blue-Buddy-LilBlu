package xyz.glabaystudios.web.crawler.ecomm.stores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;
import xyz.glabaystudios.web.model.ecomm.ShopifyProduct;
import xyz.glabaystudios.web.model.ecomm.ShopifyVariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ShopifyCrawler extends EcommCrawler {

	public ShopifyCrawler(String domain) {
		super(domain);
	}

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		System.out.println(" <-|-> \uD83D\uDCB0 <-|-> SHOPIFY <-|-> \uD83D\uDCB0 <-|->");
		ShopifyProduct storeProduct = fetchProductScript();
		if (storeProduct != null) {
			this.getPegaProduct().setProductName(storeProduct.getTitle());
			this.getPegaProduct().setProductPriceBase(getPriceFromScript(storeProduct.getPrice()));
			this.getPegaProduct().setProductDescription(storeProduct.getDescription().replaceAll("<[^>]+>", ""));
			if (storeProduct.getVariants()[0] != null) {
				this.getPegaProduct().setProductWeight(storeProduct.getVariants()[0].getWeight());
			}
			for (String option : storeProduct.getOptions()) {
				List<String> options = new ArrayList<>();
				List<String> optionPrices = new ArrayList<>();
				// loop over the variants, and add the options and prices to the product
				for (ShopifyVariant variant : storeProduct.getVariants()) {
					String varName = variant.getPublic_title();
					String varPriceDiff = decimalFormat.format(getPriceFromScript(variant.getPrice()) - this.getPegaProduct().getProductPriceBase());
					String adjustPrice = varName + " " + (formatPrice(varPriceDiff) > 0 ? "+" : "-") + varPriceDiff;
					options.add(varName);
					optionPrices.add(adjustPrice);
				}
				this.getPegaProduct().getProductOptions().put(option, options);
				this.getPegaProduct().getProductOptionPriceAdjustments().put(option, optionPrices);
			}

			Arrays.stream(storeProduct.getImages()).forEach(imageSrc -> this.getPegaProduct().getProductImages().add(imageSrc));

		} else {
			Elements productOption = page.select("select.single-option-selector");
			Elements productMisc = page.select("select.product-form__variants option");

			filterProductBasicInfo();
			addImages(page.select("div.grid.product-single img"));
			filterProductOptions(productOption);
			filterProductsForPriceAdjustments(productMisc);
		}
	}

	Double getPriceFromScript(String scriptedPrice) {
		String toFormat = scriptedPrice.substring(0, scriptedPrice.length() - 2) + "." + scriptedPrice.substring(scriptedPrice.length() - 2);
		return formatPrice(toFormat);
	}

	ShopifyProduct fetchProductScript() {
		ShopifyProduct shopifyProduct;
		ObjectMapper mapper = new ObjectMapper();
		Elements elements = page.select("div.shopify-section script");
		String json = "";
		for (Element ele : elements) {
			if (ele.attr("type").equals("application/json")) json = (ele.data()).trim();
		}
		try {
			shopifyProduct = mapper.readValue(json, ShopifyProduct.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}

		return shopifyProduct;
	}

	protected void filterProductsForPriceAdjustments(Elements extras) {
//		System.out.println("******** NEXT - MISC ELEMENTS ********");
		List<String> list = new ArrayList<>();
		extras.forEach(misc -> list.add(misc.text()));
		Collections.sort(list);
		filterOverProducts(list);
	}

	protected void filterOverProducts(List<String> list) {
		// for each option of the product
		AtomicInteger i = new AtomicInteger();
		for (String key : this.getPegaProduct().getProductOptions().keySet()) {//collect the available choices for this option
			List<String> option = this.getPegaProduct().getProductOptions().get(key);
//			System.out.println(key);
			// new lst to store the formatted choices with adjusted prices
			List<String> optionChoices = new ArrayList<>();
			// for each choice
			for (String opt : option) {
				// listing ex: "2 inch / 22 inch - $81.95 USD"
//				System.out.println(list.get(Math.min(i.get(), list.size()-1)));
				String[] listingSplit = list.get(Math.min(i.getAndIncrement(), list.size()-1)).split("-");
				String priceStr = listingSplit[1].replaceAll("[^\\d.]", ""); // remove non-numbers
				double adjustedPrice = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);// parse the price
				double adjustment = (adjustedPrice - this.getPegaProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", opt, (adjustment > 0.0 ? "+" : "-"), ("$" + decimalFormat.format(adjustment)));
				optionChoices.add(formatted);
//				System.out.println(formatted);
			}
			this.getPegaProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
//			System.out.println("OPTION " + index.getAndIncrement() + " | END");
		}
	}

	protected void filterProductOptions(Elements itemOptions) {
//		System.out.println("******** NEXT - OPTION ELEMENTS ********");
		itemOptions.forEach(option -> {
			String optionName = option.attr("data-name");
			List<String> optionChoices = new ArrayList<>();
			Elements choices = option.select("select.single-option-selector option");

			choices.forEach(choice -> optionChoices.add(choice.text()));
			Collections.sort(optionChoices);
			this.getPegaProduct().getProductOptions().put(optionName, optionChoices);
		});
	}

	protected void filterProductBasicInfo() {
		String title = page.select("h1.product-single__title").text();
		this.getPegaProduct().setProductName(title);
		checkForSale();
		this.getPegaProduct().setProductDescription(page.select("div.product-single__description").text());
	}

	private void checkForSale() {
//		System.out.println("<-|-> SHOPIFY <-|-> \uD83D\uDCB0 <-|->");
		boolean onSale = false;
		String saleTag = page.select("div.product-tag").text().toLowerCase().trim();
		if (saleTag.contains("sale")) onSale = true;
		System.out.println("Product Sale: <-|-> " + onSale);
		this.getPegaProduct().setOnSale(onSale);
		scrapePrice();
	}

	private void scrapePrice() {
		String price;
		if (this.getPegaProduct().isOnSale()) {
			price = page.select("s.product-single__price.product-single__price--compare").text();
			String salePrice = page.select("span.product-single__price").attr("content");
//			System.out.println(cleanPrice(salePrice));

			this.getPegaProduct().setProductPriceBase(price.isEmpty() ? formatPrice(cleanPrice(salePrice)) : formatPrice(cleanPrice(price)));

			this.getPegaProduct().setListedPrice(formatPrice(cleanPrice(salePrice)));
			this.getPegaProduct().setWhatYouSave((this.getPegaProduct().getProductPriceBase() - this.getPegaProduct().getListedPrice()));
		} else {
			price = page.select("div.single-product-price").attr("data-price");
			this.getPegaProduct().setProductPriceBase(formatPrice(cleanPrice(price)));
		}
//		System.out.println(cleanPrice(price));
	}
}
