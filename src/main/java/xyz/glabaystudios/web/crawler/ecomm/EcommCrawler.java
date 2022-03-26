package xyz.glabaystudios.web.crawler.ecomm;

import lombok.Getter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.Crawler;
import xyz.glabaystudios.web.model.ecomm.Product;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EcommCrawler extends Crawler {

	@Getter
	private final Product product = new Product();
	private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	public EcommCrawler(String domain) {
		super(domain, domain.contains("https://"));
	}

	@Override
	public void crawlThePageForContent() {
		if (page == null) return;
		Elements itemProps =        page.getElementsByAttribute("itemprop");
		Elements productImages =    page.select("img");
		Elements productOption =    page.select("select.single-option-selector");
		Elements productMisc =      page.select("select.product-form__variants option");

		filterProductBasicInfo(itemProps);
		filterProductImages(productImages);
		filterProductOptions(productOption);
		filterProductsForPriceAdjustments(productMisc);
	}

	private void filterProductsForPriceAdjustments(Elements extras) {
//		System.out.println("******** NEXT - MISC ELEMENTS ********");
		List<String> list = new ArrayList<>();
		extras.forEach(misc -> list.add(misc.text()));
		Collections.sort(list);
		filterOverProducts(list);
	}

	private void filterOverProducts(List<String> list) {
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
				String formatted = String.format("%s %s %s", opt, (adjustment > 0.0 ? "+" : "-"), priceStr.isEmpty() ? "Sold-Out" : ("$" + decimalFormat.format(adjustment)));
				optionChoices.add(formatted);
//				System.out.println(formatted);
			}
			getProduct().getProductOptionPriceAdjustments().put(key, optionChoices);
//			System.out.println("OPTION " + index.getAndIncrement() + " | END");
		}
	}

	private void filterProductOptions(Elements itemOptions) {
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

	private void filterProductImages(Elements productImages) {
//		System.out.println("******** NEXT - IMAGE ELEMENTS ********");
		for (Element element : productImages) {
			String ele = element.attr("src");
			if (ele.isEmpty()) continue;
//			System.out.println(ele);
			getProduct().getProductImages().add(ele);
		}
	}

	private void filterProductBasicInfo(Elements itemProperties) {
//		System.out.println("******** FIRST - ITEM-PROPERTIES ELEMENTS ********");
		if (!itemProperties.isEmpty()) {
			itemProperties.forEach(element -> {
				String ele = element.attr("itemprop");
				String value = element.text().replace("\"", "");
				if (ele.equalsIgnoreCase("name") && !value.isEmpty()) getProduct().setProductName(value);
				if (ele.equalsIgnoreCase("price") && !value.isEmpty()) getProduct().setProductPriceBase(Double.parseDouble(value.replaceAll("[^\\d.]", "")));
				if (ele.equalsIgnoreCase("description") && !value.isEmpty()) getProduct().setProductDescription(value);
				if (ele.equalsIgnoreCase("image") && !value.isEmpty()) getProduct().setProductDescription(value); // ?? do I NEED this
			});
		} else {
			getProduct().setProductName(page.select("h1.single-product-title").text());
			String priceLine = page.select("div.single-product-price").text().replaceAll("[^\\d.]", "");
			double price;
			try {
				price = Double.parseDouble(priceLine);
			} catch (NumberFormatException e) {
				price = Double.parseDouble(priceLine.substring(0, priceLine.indexOf(".") + 3));
			}
			getProduct().setProductPriceBase(Double.parseDouble(decimalFormat.format(price)));
			getProduct().setProductDescription(page.select("div.single-product-description ").text());
		}
	}
}
