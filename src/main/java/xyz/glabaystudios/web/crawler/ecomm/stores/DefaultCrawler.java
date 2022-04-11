package xyz.glabaystudios.web.crawler.ecomm.stores;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultCrawler extends EcommCrawler {

	public DefaultCrawler(String domain) {
		super(domain);
	}

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		System.out.println("<-|-> DEFAULT <-|->");
		filterProductBasicInfo();
		filterProductOptions();
		filterProductImages();
		if (this.getPegaProduct().getProductOptions().size() > 0) {
			try {
				filterProductsForPriceAdjustments();
			} catch (ParseException ignore) {
			}
		}
	}

	protected void filterProductsForPriceAdjustments() throws ParseException {
		List<String> options;

		String oneLiner = page.select("form.form-vertical script").toString();
		try {
			oneLiner = oneLiner.split("= \\{")[1].replace("</script>", "").replace("} || {};", "").trim();
			options = List.of(oneLiner.split("},"));
			filterOverProducts(options);
		} catch (Exception ignore) {
			Elements jsonData = page.select("form.variations_form.cart");
			String jsonStr = jsonData.attr("data-product_variations");

			JSONParser jsonParser = new JSONParser();
			JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonStr);
			for (String key : getPegaProduct().getProductOptions().keySet()) {
				List<String> option = this.getPegaProduct().getProductOptions().get(key);
				List<String> optionChoices = new ArrayList<>();
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonArray.get(i).toString());
					double finalPrice = formatPrice(cleanPrice(jsonObject.get("display_regular_price").toString())) - this.getPegaProduct().getProductPriceBase();
					String formatted = String.format("%s %s %s", option.get(i), (finalPrice > 0.0 ? "+" : "-"), ("$" + decimalFormat.format(finalPrice)));
					System.out.println(formatted);
					optionChoices.add(formatted);
				}
				this.getPegaProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
			}
		}

	}

	protected void filterOverProducts(List<String> list) {
		AtomicInteger i = new AtomicInteger(0);
		for (String key : this.getPegaProduct().getProductOptions().keySet()) {//collect the available choices for this option
			List<String> option = this.getPegaProduct().getProductOptions().get(key);
			List<String> optionChoices = new ArrayList<>();
			for (String opt : option) {
				if (opt.startsWith("-- ")) continue;
				String prodPrice = list.get(Math.min(i.get(), list.size()-1)).split(",")[2];
				double finalPrice = (formatPrice(cleanPrice(prodPrice)) - this.getPegaProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", opt, (finalPrice > 0.0 ? "+" : "-"), ("$" + decimalFormat.format(finalPrice)));
//				System.out.println(formatted);
				optionChoices.add(formatted);
				i.getAndIncrement();
			}
			this.getPegaProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
		}
	}

	protected void filterProductOptions() {
		Elements itemOptions = page.select("div.product-option-group.form-group");
		if (!itemOptions.isEmpty()) {
			itemOptions.forEach(option -> {
				String optionName = option.attr("data-name");
				List<String> optionChoices = new ArrayList<>();
				Elements choices = option.select("select.product-option-select option");
				if (choices.isEmpty()) choices = option.select("select.list-option-select option");
				choices.forEach(choice -> optionChoices.add(choice.text()));
				Collections.sort(optionChoices);
				this.getPegaProduct().getProductOptions().put(optionName, optionChoices);
			});
		} else {
			itemOptions = page.select("table.variations");
			itemOptions.forEach(option -> {
				String optionName = option.select("th.label").text();
				List<String> optionChoices = new ArrayList<>();
				Elements choices = option.select("td.value option");
				for (Element ele : choices) {
					String eleTxt = ele.text().toLowerCase();
					if (eleTxt.startsWith("choose") || eleTxt.startsWith("select")) continue;
					optionChoices.add(ele.text());
				}
				this.getPegaProduct().getProductOptions().put(optionName, optionChoices);
			});
		}
	}

	protected void filterProductBasicInfo() {
		addImages(page.select("div.images-wrapper img"));
		scrapeProductName();
		checkForSale();
		scrapeDescription();
		scrapeAdditionalInformation();
	}

	private void filterProductImages() {
		Elements imageElements = page.select("div.images-wrapper img");
		if (imageElements.isEmpty()) {
			imageElements = page.select("div.images img");
		}
		addImages(imageElements);
	}

	private void scrapeProductName() {
		String title = page.select("h1.single-product-title").text();
		if (title.isEmpty()) title = page.select("div.summary.entry-summary span.headline").text();

//		System.out.println("Product Title: <-|-> " + title);
		this.getPegaProduct().setProductName(title);
	}

	private void scrapeDescription() {
		String description = page.select("div.single-product-description ").text();
		StringBuilder descBuilder = new StringBuilder();
		if (description.isEmpty()) {
			descBuilder.append(page.select("div.woocommerce-product-details__short-description").text()).append("\n");
			descBuilder.append(page.select("div.tabPane").select("div.bt_bb_wrapper").text());
			description = descBuilder.toString().trim();
		}

//		System.out.println("Product Description: <-|-> " + description);
		this.getPegaProduct().setProductDescription(description);
	}

	private void scrapeAdditionalInformation() {
		Elements additional = page.select("table.woocommerce-product-attributes.shop_attributes");

		for (Element ele : additional) {
			String header = ele.select("th.woocommerce-product-attributes-item__label").text();
			String data = ele.select("td.woocommerce-product-attributes-item__value").text();
			if (header.equalsIgnoreCase("weight")) getPegaProduct().setProductWeight(data);

//			System.out.println("Product Additional Information: <-|-> " + header + " | " + data);
		}
	}

	private void checkForSale() {
//		System.out.println("<-|-> \uD83D\uDCB0 <-|->");
		String saleString = page.select("div.single-product-price").attr("data-on-sale");
		boolean onSale = false;
		if (!saleString.isEmpty() || !saleString.isBlank()) onSale = Boolean.parseBoolean(saleString);
//		System.out.println("Product Sale: <-|-> " + onSale);
		this.getPegaProduct().setOnSale(onSale);
		scrapePrice();
	}

	private void scrapePrice() {
		String price = page.select("div.single-product-price").attr("data-price");
		if (price.isEmpty()) price = page.select("p.price span.woocommerce-Price-amount.amount").text();
		this.getPegaProduct().setProductPriceBase(formatPrice(cleanPrice(price)));
		if (this.getPegaProduct().isOnSale()) {
			String salePrice = page.select("div.single-product-price").attr("data-price-sale");

			this.getPegaProduct().setListedPrice(formatPrice(cleanPrice(salePrice)));
			this.getPegaProduct().setWhatYouSave((this.getPegaProduct().getProductPriceBase() - this.getPegaProduct().getListedPrice()));
		}
//		System.out.println(getProduct().getProductPriceBase());
	}
}
