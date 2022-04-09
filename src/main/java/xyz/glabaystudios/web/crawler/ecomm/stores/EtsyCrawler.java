package xyz.glabaystudios.web.crawler.ecomm.stores;

import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.util.ArrayList;
import java.util.List;

public class EtsyCrawler extends EcommCrawler {

	public EtsyCrawler(String domain) {
		super(domain);
	}

	private final ArrayList<String> optionNames = new ArrayList<>();

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		filterProductBasicInfo();
		filterProduct();
		addImages(page.select("div.image-carousel-container img"));
	}

	protected void filterProduct() {
		ArrayList<String> list;
		Elements options = page.select("div.wt-validation label.wt-display-block");
		options.forEach(option -> optionNames.add(option.text()));

		Elements optionsAvail = page.select("div.wt-validation select.wt-select__element");
		for (int index = 0; index < optionNames.size(); index++) {
			list = filterOverProducts(optionsAvail.get(index).select("select.wt-select__element option").eachText());
			this.getPegaProduct().getProductOptions().put(optionNames.get(index), list);
		}

		//filter the options price adjustments
		for (int index = 0; index < optionNames.size(); index++) {
			list = filterProductsForPriceAdjustments(optionsAvail.get(index).select("select.wt-select__element option").eachText());
			this.getPegaProduct().getProductOptionPriceAdjustments().put(optionNames.get(index), list);
		}
	}

	protected ArrayList<String> filterProductsForPriceAdjustments(List<String> list) {
		ArrayList<String> formattedResult = new ArrayList<>();
		for (String str : list) {
			if (str.equalsIgnoreCase("Select an option")) continue;
			if (str.contains("$") && str.contains("(")) {
				String priceStr = str.split("\\(")[1].replaceAll("[^\\d.]", ""); // remove non-numbers
				double adjustedPrice = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);// parse the price
				String priceStr2 = page.select("div.wt-mb-xs-3 p.wt-text-slime").text().split("\\(")[0].replaceAll("[^\\d.]", "");
				boolean sale = !priceStr2.isEmpty();
				double adjustment = sale ? ((adjustedPrice + Double.parseDouble(priceStr2)) - this.getPegaProduct().getProductPriceBase()) : (adjustedPrice - this.getPegaProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", str.split("\\(")[0].trim(), (adjustment > 0.0 ? "+" : "-"), priceStr.isEmpty() ? "Sold-Out" : ("$" + decimalFormat.format(adjustment)));
				formattedResult.add(formatted);
			}
		}
		return formattedResult;
	}

	protected ArrayList<String> filterOverProducts(List<String> list) {
		ArrayList<String> formattedResult = new ArrayList<>();
		for (String str : list) {
			if (str.equalsIgnoreCase("Select an option")) continue;
			if (str.contains("$") && str.contains("(")) formattedResult.add(str.split("\\(")[0]);
			else formattedResult.add(str);
		}
		return formattedResult;
	}

	protected void filterProductBasicInfo() {
		String title = page.select("div.cart-col h1.wt-text-body-03.wt-line-height-tight.wt-break-word").text(); // product name
		this.getPegaProduct().setProductName(title);

		double price = scrapePrice();
		this.getPegaProduct().setProductPriceBase(price);
		this.getPegaProduct().setProductDescription(page.select("div.listing-info p.wt-text-body-01").eachText().get(0));
	}

	private double scrapePrice() {
		List<String> priceOptions = page.select("div.cart-col span.wt-screen-reader-only").eachText();
		System.out.println(priceOptions);
		String priceStr = "";
		for (String price : priceOptions) {
			if (price.startsWith("Original")) priceStr = cleanPrice(page.select("div.cart-col p.wt-text-strikethrough").text());
			if (price.startsWith("Price")) priceStr = cleanPrice(page.select("div.cart-col p.wt-text-title-03").text());
		}
		if (priceStr.isEmpty()) priceStr = cleanPrice(page.select("div.wt-mb-xs-3 p.wt-text-title-03").text());

		return formatPrice(priceStr);
	}
}
