package xyz.glabaystudios.web.crawler.ecomm.stores;

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
		Elements productOption =    page.select("div.product-option-group.form-group");

		filterProductBasicInfo();
		addImages(page.select("div.images-wrapper img"));
		filterProductOptions(productOption);
		filterProductsForPriceAdjustments();
	}

	protected void filterProductsForPriceAdjustments() {
		String oneLiner = page.select("form.form-vertical script").toString()
				.split("= \\{")[1]
				.replace("</script>", "")
				.replace("} || {};", "")
				.trim();
//		System.out.println(oneLiner);
		List<String> options = List.of(oneLiner.split("},"));
//		Collections.sort(options);
		filterOverProducts(options);
	}

	protected void filterOverProducts(List<String> list) {
		// for each option of the product
		AtomicInteger i = new AtomicInteger(0);
		for (String key : getProduct().getProductOptions().keySet()) {//collect the available choices for this option
			List<String> option = getProduct().getProductOptions().get(key);
//			System.out.println(key);
			// new lst to store the formatted choices with adjusted prices
			List<String> optionChoices = new ArrayList<>();
//			System.out.println("<-|-> " + list);
			for (String opt : option) {
				System.out.println(opt);
				if (opt.startsWith("-- ")) continue;
				System.out.println(list.get(Math.min(i.get(), list.size()-1)));
				String prodPrice = list.get(Math.min(i.get(), list.size()-1)).split(",")[2];
				double finalPrice = (formatPrice(cleanPrice(prodPrice)) - getProduct().getProductPriceBase());
				String formatted = String.format("%s %s %s", opt, (finalPrice > 0.0 ? "+" : "-"), ("$" + decimalFormat.format(finalPrice)));

//				System.out.println(formatted);
				optionChoices.add(formatted);
				i.getAndIncrement();
			}
			getProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
		}
	}

	protected void filterProductOptions(Elements itemOptions) {
//		System.out.println("******** NEXT - OPTION ELEMENTS ********");
		itemOptions.forEach(option -> {
			String optionName = option.attr("data-name");
//			System.out.println("Product OptionName: <-|-> " + optionName);
			List<String> optionChoices = new ArrayList<>();
			Elements choices = option.select("select.product-option-select option");
//			System.out.println("Product Options: <-|-> " + choices);
			if (choices.isEmpty()) {
				choices = option.select("select.list-option-select option");
//				System.out.println("Product Options-two: <-|-> " + choices);
			}
			choices.forEach(choice -> optionChoices.add(choice.text()));
			Collections.sort(optionChoices);
			getProduct().getProductOptions().put(optionName, optionChoices);
		});
	}

	protected void filterProductBasicInfo() {
		String title = page.select("h1.single-product-title").text();
//		System.out.println("Product Title: <-|-> " + title);
		getProduct().setProductName(title);

		checkForSale();
		getProduct().setProductDescription(page.select("div.single-product-description ").text());
	}

	private void checkForSale() {
//		System.out.println("<-|-> \uD83D\uDCB0 <-|->");
		String saleString = page.select("div.single-product-price").attr("data-on-sale");
		boolean onSale = false;
		if (!saleString.isEmpty() || !saleString.isBlank()) onSale = Boolean.parseBoolean(saleString);
//		System.out.println("Product Sale: <-|-> " + onSale);
		getProduct().setOnSale(onSale);
		scrapePrice();
	}

	private void scrapePrice() {
		String price = page.select("div.single-product-price").attr("data-price");
		getProduct().setProductPriceBase(formatPrice(cleanPrice(price)));
		if (getProduct().isOnSale()) {
			String salePrice = page.select("div.single-product-price").attr("data-price-sale");

			getProduct().setListedPrice(formatPrice(cleanPrice(salePrice)));
			getProduct().setWhatYouSave((getProduct().getProductPriceBase() - getProduct().getListedPrice()));
		}
//		System.out.println(getProduct().getProductPriceBase());
	}
}
