package xyz.glabaystudios.web.crawler;

import org.jsoup.nodes.Document;

public abstract class Crawler implements WebsitePageCrawler {

	protected final Document page;
	protected static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";
	protected String domain;

	protected Crawler(String domain, boolean usingSecureConnection) {
		this.domain = domain
				.toLowerCase()
				.replace("http://", "")
				.replace("https://", "");
		page = getContent(this.domain, userAgent, usingSecureConnection);
	}
}