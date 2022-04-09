package xyz.glabaystudios.web.crawler.ecomm;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.Crawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.AmazonCrawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.DefaultCrawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.EtsyCrawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.ShopifyCrawler;
import xyz.glabaystudios.web.model.ecomm.PegaProduct;

import java.io.IOException;
import java.text.DecimalFormat;

public abstract class EcommCrawler extends Crawler {

	@Getter
	protected final PegaProduct pegaProduct = new PegaProduct();
	protected final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	public EcommCrawler(String domain) {
		super(domain, domain.contains("https://"));
	}

	public static EcommCrawler getCrawlingMerchant(String domain) {
		try {
			Document page = Jsoup.connect(domain).userAgent(userAgent).timeout(42000).get();
			boolean isShopify = !page.select("div.shopify-section").isEmpty();
			if (isShopify) return new ShopifyCrawler(domain);
			else if (domain.contains("etsy.com")) return new EtsyCrawler(domain);
			else if (domain.contains("amazon.com")) return new AmazonCrawler(domain);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new DefaultCrawler(domain);
	}

	/**
	 * Filter Product basic Information
	 * <br><p>
	 *     This should be looking for very basic information about the product.
	 *     Such as the Name, Base Price, Description.
	 *     Each concrete child of {@link EcommCrawler} should handle their own way of getting
	 *     the remaining information: options, price adjustments, images, weights, sizes.
	 * </p>
	 */
	protected abstract void filterProductBasicInfo();

	/**
	 * Format Price:<br>
	 * This will take an unformatted String and parse it to the Double Object
	 * @param unformatted the unformatted string
	 * @return A formatted price with no other characters
	 */
	protected double formatPrice(String unformatted) {
		return Double.parseDouble(cleanPrice(unformatted));
	}

	/**
	 * This will take a dirtyString String and remove all non-numerical
	 * @param dirtyString the dirty String
	 * @return A cleaned String with no non-numerical characters
	 */
	protected String cleanPrice(String dirtyString) {
		return dirtyString.replaceAll("[^\\d.]", "");
	}

	/**
	 * Taking in a collection of {@link Elements}, we will filter over the empty ones and add the image links to the {@link PegaProduct}
	 * @param elements the Elements to filter
	 */
	protected void addImages(Elements elements) {
		elements.stream().map(element -> element.attr("src")).filter(ele -> !ele.isEmpty()).forEach(ele -> this.getPegaProduct().getProductImages().add(ele));
	}

}
