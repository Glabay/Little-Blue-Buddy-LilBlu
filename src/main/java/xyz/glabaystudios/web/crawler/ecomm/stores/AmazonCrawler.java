package xyz.glabaystudios.web.crawler.ecomm.stores;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.util.ArrayList;
import java.util.List;

public class AmazonCrawler extends EcommCrawler {

	public AmazonCrawler(String domain) {
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
		Elements elements = page.select("ul.regularAltImageViewLayout img");
		elements.forEach(element -> {
			String imageLink = element.attr("src");
			if (!imageLink.isBlank() || !imageLink.isEmpty()) getProduct().getProductImages().add(imageLink);
		});
	}

	protected void filterProduct() {
		ArrayList<String> list;
		Elements optionName = page.select("div.a-section div.a-row label.a-form-label");
		for (Element option : optionName) {
			list = new ArrayList<>();
			System.out.println(option.text());
			optionNames.add(option.text());
			Elements optionsAvail = page.select("div.a-section ul.swatchesSquare li");
			for (Element opt : optionsAvail) {
				String str = opt.attributes().get("id");
				String compare = option.text().replace(" ", "_").replace(":", "").toLowerCase();
				if (str.contains(compare)) {
					list.add(opt.text());
				}
			}
			getProduct().getProductOptions().put(option.text(), list);
		}

//		Elements optionsAvail = page.select("div.a-section ul.swatchesSquare li");
//		for (String name : optionNames) {
//			System.out.println(name);
//			list = filterOverProducts(optionsAvail.eachText());
//			getProduct().getProductOptions().put(name, list);
//		}
//
//		//filter the options price adjustments
//		for (int index = 0; index < optionNames.size(); index++) {
//			System.out.println(optionsAvail.eachText());
//			list = filterProductsForPriceAdjustments(optionsAvail.eachText());
//			getProduct().getProductOptionPriceAdjustments().put(optionNames.get(index), list);
//		}
	}

	protected ArrayList<String> filterProductsForPriceAdjustments(List<String> list) {
		ArrayList<String> formattedResult = new ArrayList<>();
		for (String str : list) {
			if (str.contains("$")) {
				String priceStr = str.split("\\$")[1].replaceAll("[^\\d.]", ""); // remove non-numbers
				double adjustedPrice = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);// parse the price
				double adjustment = getProduct().isOnSale() ? ((adjustedPrice + getProduct().getWhatYouSave()) - getProduct().getProductPriceBase()) : (adjustedPrice - getProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", str.split("\\$")[1].trim(), (adjustment > 0.0 ? "+" : "-"), priceStr.isEmpty() ? "Sold-Out" : ("$" + decimalFormat.format(adjustment)));
				System.out.println(formatted);
				formattedResult.add(formatted);
			}
		}
		return formattedResult;
	}

	protected ArrayList<String> filterOverProducts(List<String> list) {
		ArrayList<String> formattedResult = new ArrayList<>();
		for (String str : list) {
			if (str.contains("$")) formattedResult.add(str.split("\\$")[1]);
			else formattedResult.add(str);
		}
		return formattedResult;
	}

	protected void filterProductBasicInfo() {
		String title = page.select("div.a-section h1").get(0).text(); // product name
		getProduct().setProductName(title);

		double price = scrapePrice();
		getProduct().setProductPriceBase(price);
		String amazonDescription = page.select("div.celwidget table.a-normal").get(0).text() + "\n"
								 + page.select("div.celwidget ul.a-spacing-mini li span.a-list-item").text();
		getProduct().setProductDescription(amazonDescription);
	}

	private double scrapePrice() {
		String priceStr = "0.0";
		Elements corePrice = page.select("div.celwidget");
		for (Element element : corePrice) {
			Element temp = element.getElementById("corePrice_desktop");
			if (temp != null && !temp.text().isEmpty()) {
				List<String> text = temp.select("td.a-nowrap").eachText();
				for (String opt : text) {
					String tempStr = opt.replace(":", "").toLowerCase();
					if (tempStr.equalsIgnoreCase("list price") && getProduct().getListedPrice() == 0.0) getProduct().setListedPrice(formatPrice(temp.select("span.a-offscreen").get(0).text()));
					if (tempStr.equalsIgnoreCase("price") && priceStr.equals("0.0")) priceStr = String.valueOf(formatPrice(temp.select("span.a-offscreen").get(1).text()));
					if (tempStr.equalsIgnoreCase("you save") && getProduct().getWhatYouSave() == 0.0) {
						getProduct().setWhatYouSave(formatPrice(temp.select("span.a-offscreen").get(2).text()));
						getProduct().setOnSale(true);
					}
				}
			}
		}
		return Double.parseDouble(priceStr);
	}

	private double formatPrice(String unformatted) {
		return Double.parseDouble(unformatted.replaceAll("[^\\d.]", ""));
	}
}
