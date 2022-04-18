package xyz.glabaystudios.web.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xyz.glabaystudios.net.NetworkExceptionHandler;

import java.io.IOException;
import java.net.UnknownHostException;

public interface WebsitePageCrawler {

	void crawlThePageForContent();

	default Document getContent(String domain, String userAgent, boolean usingSecureConnection) {
		try {
			return Jsoup.connect((usingSecureConnection ? "https://" : "http://") + domain)
					.userAgent(userAgent)
					.timeout(42000)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.get();
		} catch (UnknownHostException e) {
			NetworkExceptionHandler.handleException("getContent -> UnknownHost " + domain, e);
			return null;
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("getContent -> InputOutput " + domain, e);
			return null;
		}
	}
}
