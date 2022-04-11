package xyz.glabaystudios.web.crawler.ecomm.stores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;
import xyz.glabaystudios.web.model.ecomm.wix.WixCatalog;
import xyz.glabaystudios.web.model.ecomm.wix.WixProductAdditionInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WixCrawler extends EcommCrawler {

	public WixCrawler(String domain) {
		super(domain);
	}

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		System.out.println(" <-|-> \uD83D\uDCB0 <-|-> WIX-SHOP <-|-> \uD83D\uDCB0 <-|->");
		WixCatalog storeProduct = fetchProductScript();
		if (storeProduct != null) {
			System.out.println(storeProduct);

			this.getPegaProduct().setProductName(storeProduct.getProduct().getName());
			this.getPegaProduct().setProductPriceBase(storeProduct.getProduct().getPrice());
			String description = storeProduct.getProduct().getDescription().replaceAll("<[^>]+>", "");
			if (description.isEmpty()) {
				StringBuilder descBuilder = new StringBuilder();
				for (WixProductAdditionInfo additionalInfo : storeProduct.getProduct().getAdditionalInfo()) {
					descBuilder.append(additionalInfo.getTitle()).append("\n");
					descBuilder.append(additionalInfo.getDescription().replaceAll("<[^>]+>", "").trim()).append("\n");
				}
				description = descBuilder.toString();
			}
			this.getPegaProduct().setProductDescription(description);
			this.getPegaProduct().setProductWeight(String.valueOf(storeProduct.getProduct().getWeight()));

			for (String option : storeProduct.getProduct().getOptions()) {
				List<String> options = new ArrayList<>();
				List<String> optionPrices = new ArrayList<>();
				// TODO: Find wixsite with Store, and products with options
				// TODO: loop over the variants, and add the options and prices to the product
//				for (WixProduct variant : storeProduct.getProduct().getOptions()) {
//					String varName = variant.getPublic_title();
//					String varPriceDiff = decimalFormat.format(getPriceFromScript(variant.getPrice()) - this.getPegaProduct().getProductPriceBase());
//					String adjustPrice = varName + " " + (formatPrice(varPriceDiff) > 0 ? "+" : "-") + varPriceDiff;
//					options.add(varName);
//					optionPrices.add(adjustPrice);
//				}
				this.getPegaProduct().getProductOptions().put(option, options);
				this.getPegaProduct().getProductOptionPriceAdjustments().put(option, optionPrices);
			}

			Arrays.stream(storeProduct.getProduct().getMedia()).forEach(imageSrc -> this.getPegaProduct().getProductImages().add(imageSrc.getFullUrl()));


		} else {
			Elements productOption = page.select("select.single-option-selector");
			Elements productMisc = page.select("select.product-form__variants option");

			filterProductBasicInfo();
			addImages(page.select("div.grid.product-single img"));
			filterProductOptions(productOption);
			filterProductsForPriceAdjustments(productMisc);
		}
	}

	WixCatalog fetchProductScript() {
		WixCatalog wixProduct = null;
		ObjectMapper mapper = new ObjectMapper();
		Elements elements = page.getElementsByTag("script");
		String json = "";
		for (Element ele : elements) {
			if (ele.attr("type").equals("application/json") && ele.attr("id").equals("wix-warmup-data")) json = (ele.data()).trim();
		}

		try {
			JSONObject object = (JSONObject) new JSONParser().parse(json);
			JSONObject obj = (JSONObject) object.get("appsWarmupData");

			Set<?> set = obj.keySet();
			for (Object ob : set) {
				String key = String.valueOf(ob);
				JSONObject obj2 = (JSONObject) obj.get(key);
				Set<?> set2 = obj2.keySet();
				json = set2.stream()
						.map(String::valueOf)
						.map(key2 -> (JSONObject) obj2.get(key2))
						.map(obj3 -> (JSONObject) obj3.get("catalog"))
						.findFirst()
						.map(JSONAware::toJSONString)
						.orElse(json);
			}

			wixProduct = mapper.readValue(json, WixCatalog.class);
		} catch (ParseException e) {
			NetworkExceptionHandler.handleException("fetchProductScript -> Parse ", e);
		} catch (JsonProcessingException e) {
			NetworkExceptionHandler.handleException("fetchProductScript -> JsonProcessing ", e);
		}
		return wixProduct;
	}

	protected void filterProductsForPriceAdjustments(Elements extras) {
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
			List<String> optionChoices = new ArrayList<>();
			for (String opt : option) {
				String[] listingSplit = list.get(Math.min(i.getAndIncrement(), list.size()-1)).split("-");
				String priceStr = listingSplit[1].replaceAll("[^\\d.]", ""); // remove non-numbers
				double adjustedPrice = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);// parse the price
				double adjustment = (adjustedPrice - this.getPegaProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", opt, (adjustment > 0.0 ? "+" : "-"), ("$" + decimalFormat.format(adjustment)));
				optionChoices.add(formatted);
			}
			this.getPegaProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
		}
	}

	protected void filterProductOptions(Elements itemOptions) {
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
