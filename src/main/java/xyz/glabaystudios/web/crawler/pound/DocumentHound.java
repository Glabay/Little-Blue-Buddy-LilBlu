package xyz.glabaystudios.web.crawler.pound;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.crawler.WebsitePageCrawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class DocumentHound implements Runnable, WebsitePageCrawler {

	String name;

	final String domainHome;
	String domainPage;
	Document page;
	final String userAgent = "Mozilla/5.0 (Windows NT 6.1; rv:80.0) Gecko/27132701 Firefox/78.7";

	HashMap<String, String> foundDocuments = new HashMap<>();

	protected boolean searchingForDocx;
	protected boolean searchingForPdf;
	protected boolean searchingForVideos;
	protected boolean searchingForPpt;

	public DocumentHound(String domainHome) {
		this.domainHome = domainHome;
	}

	public void setTarget(String pageTarget) {
		this.domainPage = pageTarget;
		if (domainPage.endsWith("/")) this.domainPage = domainPage.substring(0, domainPage.length()-1);
	}

	public void setTargetDocuments(boolean docx, boolean pdf, boolean videos, boolean ppt) {
		searchingForDocx = docx;
		searchingForPdf = pdf;
		searchingForVideos = videos;
		searchingForPpt = ppt;
	}

	public void getPageContent() {
		page = getContent();
	}

	Document getContent() {
		try { // try it with the Secure first
			Connection jsoupConnection = Jsoup.connect(domainPage);

			jsoupConnection.userAgent(userAgent);
			jsoupConnection.timeout(20000);
			jsoupConnection.ignoreContentType(true);
			jsoupConnection.ignoreHttpErrors(true);

			return jsoupConnection.get();
		} catch (HttpStatusException e) {
			NetworkExceptionHandler.handleException("getContent -> " + domainPage, e);
			return null;
		} catch (SocketTimeoutException e) {
//			NetworkExceptionHandler.handleException("getContent -> SocketTimeout " + domainPage, e);
			return null;
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("getContent -> InputOutput", e);
			return null;
		}
	}

	public void run() {
		sniffForLinks();
	}

	protected String[] documentType   = { ".doc",      ".docm",     ".docx"   };
	protected String[] powerPointType = { ".potx",     ".ppt",     ".pptx"   };

	public void sniffForLinks() {
		System.out.println(getName() + " is Sniffing...");
		if (page == null) return;
		Elements links = page.getElementsByAttribute("href");
		for (Element link : links) {
			String href = link.attr("href");
			applyFilter(href);
		}
	}

	public HashMap<String, String> getFoundDocuments() {
		getPageContent();
		if (page == null) return null;
//		System.out.println(getName() + " is Sniffing...");
		Elements links = page.getElementsByAttribute("href");
		for (Element link : links) {
			String href = link.attr("href");
			applyFilter(href);
		}
		return foundDocuments;
	}

	protected void applyFilter(String link) {
		if (searchingForPdf && link.endsWith(".pdf")) {
			foundDocuments.put(link, "PDF");
			return;
		}
		if (searchingForPpt) {
			for (String ext : powerPointType) {
				if (link.endsWith(ext)) {
					foundDocuments.put(link, "PPT");
					return;
				}
			}
		}
		if (searchingForDocx) {
			for (String ext : documentType) {
				if (link.endsWith(ext)) {
					foundDocuments.put(link, "Document");
					return;
				}
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	protected String getName() {
		return name;
	}
}
