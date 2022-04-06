package xyz.glabaystudios.web.crawler.ecomm.stores;

import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.util.ArrayList;
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
		Elements productOption =    page.select("select.single-option-selector");
		Elements productMisc =      page.select("select.product-form__variants option");

		filterProductBasicInfo();
		addImages(page.select("div.grid.product-single img"));
		filterProductOptions(productOption);
		filterProductsForPriceAdjustments(productMisc);
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
		for (String key : getProduct().getProductOptions().keySet()) {//collect the available choices for this option
			List<String> option = getProduct().getProductOptions().get(key);
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
				double adjustment = (adjustedPrice - getProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", opt, (adjustment > 0.0 ? "+" : "-"), ("$" + decimalFormat.format(adjustment)));
				optionChoices.add(formatted);
//				System.out.println(formatted);
			}
			getProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
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
			getProduct().getProductOptions().put(optionName, optionChoices);
		});
	}

	protected void filterProductBasicInfo() {
		String title = page.select("h1.product-single__title").text();
		getProduct().setProductName(title);
		checkForSale();
		getProduct().setProductDescription(page.select("div.product-single__description").text());
	}

	private void checkForSale() {
//		System.out.println("<-|-> SHOPIFY <-|-> \uD83D\uDCB0 <-|->");
		boolean onSale = false;
		String saleTag = page.select("div.product-tag").text().toLowerCase().trim();
		if (saleTag.contains("sale")) onSale = true;
		System.out.println("Product Sale: <-|-> " + onSale);
		getProduct().setOnSale(onSale);
		scrapePrice();
	}

	private void scrapePrice() {
		String price;
		if (getProduct().isOnSale()) {
			price = page.select("s.product-single__price.product-single__price--compare").text();
			String salePrice = page.select("span.product-single__price").attr("content");
//			System.out.println(cleanPrice(salePrice));

			getProduct().setProductPriceBase(price.isEmpty() ? formatPrice(cleanPrice(salePrice)) : formatPrice(cleanPrice(price)));

			getProduct().setListedPrice(formatPrice(cleanPrice(salePrice)));
			getProduct().setWhatYouSave((getProduct().getProductPriceBase() - getProduct().getListedPrice()));
		} else {
			price = page.select("div.single-product-price").attr("data-price");
			getProduct().setProductPriceBase(formatPrice(cleanPrice(price)));
		}
//		System.out.println(cleanPrice(price));
	}
}
