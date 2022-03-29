package xyz.glabaystudios.web.crawler.ecomm.stores;

import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.util.ArrayList;
import java.util.List;

public class EtsyCrawler extends EcommCrawler {

	public EtsyCrawler(String domain) {
		super(domain);
	}

	private ArrayList<String> optionNames = new ArrayList<>();

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		filterProductBasicInfo();
		filterProduct();
		filterProductImages();
	}

	protected void filterProductImages() {
		Elements elements = page.select("li.wt-mr-xs-1 img");
		Elements imageElements = page.select("img");

		elements.forEach(element -> {
			String imageLink = element.attr("src");
			if (!imageLink.isBlank() || !imageLink.isEmpty()) getProduct().getProductImages().add(imageLink);
		});
	}

	protected void filterProduct() {
		ArrayList<String> list;
		Elements options = page.select("div.wt-validation label.wt-display-block");
		options.forEach(option -> optionNames.add(option.text()));

		Elements optionsAvail = page.select("div.wt-validation select.wt-select__element");
		for (int index = 0; index < optionNames.size(); index++) {
			list = filterOverProducts(optionsAvail.get(index).select("select.wt-select__element option").eachText());
			getProduct().getProductOptions().put(optionNames.get(index), list);
		}

		//filter the options price adjustments
		for (int index = 0; index < optionNames.size(); index++) {
			list = filterProductsForPriceAdjustments(optionsAvail.get(index).select("select.wt-select__element option").eachText());
			getProduct().getProductOptionPriceAdjustments().put(optionNames.get(index), list);
		}
	}

	protected ArrayList<String> filterProductsForPriceAdjustments(List<String> list) {
		ArrayList<String> formattedResult = new ArrayList<>();
		for (String str : list) {
			if (str.equalsIgnoreCase("Select an option")) continue;
			if (str.contains("$") && str.contains("(")) {
				String priceStr = str.split("\\(")[1].replaceAll("[^\\d.]", ""); // remove non-numbers
				double adjustedPrice = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);// parse the price
				double saleValue = Double.parseDouble(page.select("div.wt-mb-xs-3 p.wt-text-slime").text().split("\\(")[0].replaceAll("[^\\d.]", ""));
				boolean sale = saleValue > 0.0;
				double adjustment = sale ? ((adjustedPrice + saleValue) - getProduct().getProductPriceBase()) : (adjustedPrice - getProduct().getProductPriceBase());
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
		String title = page.select("div.cart-col h1").text(); // product name
		getProduct().setProductName(title);

		double price = scrapePrice();
		getProduct().setProductPriceBase(price);
		getProduct().setProductDescription(page.select("div.listing-info p.wt-text-body-01").eachText().get(0));
	}

	private double scrapePrice() {
		List<String> priceOptions = page.select("div.cart-col span.wt-screen-reader-only").eachText();
		System.out.println(priceOptions);
		String priceStr = "";
		for (String price : priceOptions) {
			if (price.startsWith("Original")) priceStr = (page.select("div.cart-col p.wt-text-strikethrough").text().replaceAll("[^\\d.]", ""));
			if (price.startsWith("Price")) priceStr = (page.select("div.cart-col p.wt-text-title-03").text().replaceAll("[^\\d.]", ""));
		}
		if (priceStr.isEmpty()) {
			priceStr = (page.select("div.wt-mb-xs-3 p.wt-text-title-03").text().replaceAll("[^\\d.]", ""));
		}
		return Double.parseDouble(priceStr);
	}
}
