package xyz.glabaystudios.web.crawler.ecomm;

import lombok.Getter;
import xyz.glabaystudios.web.crawler.Crawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.AmazonCrawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.DefaultCrawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.EtsyCrawler;
import xyz.glabaystudios.web.crawler.ecomm.stores.ShopifyCrawler;
import xyz.glabaystudios.web.model.ecomm.Product;

import java.text.DecimalFormat;

public abstract class EcommCrawler extends Crawler {

	@Getter
	protected final Product product = new Product();
	protected final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	public EcommCrawler(String domain) {
		super(domain, domain.contains("https://"));
	}

	public static EcommCrawler getCrawlingMerchant(String domain) {
		if (domain.contains("shopify.com")) return new ShopifyCrawler(domain);
		else if (domain.contains("etsy.com")) return new EtsyCrawler(domain);
		else if (domain.contains("amazon.com")) return new AmazonCrawler(domain);
		else return new DefaultCrawler(domain);
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

}
