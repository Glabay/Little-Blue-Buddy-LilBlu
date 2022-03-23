package xyz.glabaystudios.web.crawler;

import org.jsoup.nodes.Document;

public abstract class Crawler implements WebsitePageCrawler {

	protected final Document page;
	protected final String userAgent = "Mozilla/5.0 (Windows NT 6.1; rv:80.0) Gecko/27132701 Firefox/78.7";
	protected String domain;

	protected Crawler(String domain, boolean usingSecureConnection) {
		this.domain = domain
				.toLowerCase()
				.replace("http://", "")
				.replace("https://", "");
		page = getContent(this.domain, userAgent, usingSecureConnection);
	}
}