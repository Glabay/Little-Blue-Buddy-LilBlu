package xyz.glabaystudios.web.crawler.ecomm.stores;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AmazonCrawler extends EcommCrawler {

	public AmazonCrawler(String domain) {
		super(domain);
	}

	private final ArrayList<String> optionNames = new ArrayList<>();

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		filterProductBasicInfo();
		filterProduct();
		addImages(page.select("ul.regularAltImageViewLayout img"));
	}

	protected void filterProduct() {
		ArrayList<String> list;
		Elements optionName = page.select("div.a-section div.a-row label.a-form-label");
		Elements optionsAvail = page.select("div.a-section ul.swatchesSquare li");

		for (Element option : optionName) {
			list = new ArrayList<>();
			System.out.println(option.text());
			optionNames.add(option.text());
			for (Element opt : optionsAvail) {
				String str = opt.attributes().get("id");
				String compare = option.text().replace(" ", "_").replace(":", "").toLowerCase();
				if (str.contains(compare)) list.add(opt.text());
			}
			getProduct().getProductOptions().put(option.text(), list);
		}
//		//filter the options price adjustments
		IntStream.range(0, optionNames.size()).forEach(this::filterProductsForPriceAdjustments);
	}

	@SuppressWarnings("unchecked")
	protected void filterProductsForPriceAdjustments(int index) {
		ArrayList<String> formattedResult = new ArrayList<>();
		ArrayList<String> products = (ArrayList<String>) getProduct().getProductOptions().values().toArray()[index];
		for (int i = 0; i < products.size(); i++) {
			String str = getProduct().getProductOptions().get(optionNames.get(index)).get(i);
			if (str.contains("$")) {
				String priceStr = cleanPrice(str.split("\\$")[1]); // remove non-numbers
				double adjustedPrice = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);// parse the price
				double adjustment = getProduct().isOnSale() ? ((adjustedPrice + getProduct().getWhatYouSave()) - getProduct().getProductPriceBase()) : (adjustedPrice - getProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", str.split("\\$")[0].trim(), (adjustment > 0.0 ? "+" : "-"), priceStr.isEmpty() ? "Sold-Out" : ("$" + decimalFormat.format(adjustment)));
				System.out.println(formatted);
				formattedResult.add(formatted);
			}
		}
		getProduct().getProductOptionPriceAdjustments().put(optionNames.get(index), formattedResult);
	}

	protected void filterProductBasicInfo() {
		String title = page.select("div.a-section span.product-title-word-break").text(); // product name
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
			Element temp = element.getElementById("apex_desktop");
			if (temp != null && !temp.text().isEmpty()) {
				List<String> text = temp.select("td.a-color-secondary.a-size-base.a-text-right.a-nowrap").eachText();
				if (text.size() == 1) priceStr = String.valueOf(formatPrice(temp.select("span:nth-of-type(2)").text()));
				else {
					Elements table = temp.select("table.a-lineitem.a-align-top");
					Element tableData = table.get(0);
					getProduct().setListedPrice(formatPrice(tableData.select("tr:nth-of-type(1)").select("span:nth-of-type(2)").text()));
					priceStr = String.valueOf(formatPrice(tableData.select("tr:nth-of-type(2)").select("span:nth-of-type(2)").text()));
					getProduct().setWhatYouSave(formatPrice(tableData.select("tr:nth-of-type(3)").select("span:nth-of-type(2)").text()));
					getProduct().setOnSale(true);
				}
			}
		}
		return formatPrice(priceStr);
	}
}
