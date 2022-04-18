package xyz.glabaystudios.web.crawler.pound;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.crawler.WebsitePageCrawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;

public class DocumentHound implements Runnable, WebsitePageCrawler {

	@Getter @Setter
	protected String name;

	final String domainHome;

	String domainPage;
	Document page;
	final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";

	HashMap<String, String> foundDocuments = new HashMap<>();

	protected boolean searchingForDocx;
	protected boolean searchingForPdf;
	protected boolean searchingForVideos;
	protected boolean searchingForPpt;
	protected boolean searchingForEmbed;

	public DocumentHound(String domainHome) {
		this.domainHome = domainHome;
	}

	public void setTarget(String pageTarget) {
		this.domainPage = pageTarget;
		if (domainPage.endsWith("/")) this.domainPage = domainPage.substring(0, domainPage.length()-1);
	}

	public void setTargetDocuments(boolean docx, boolean pdf, boolean videos, boolean ppt, boolean embed) {
		searchingForDocx = docx;
		searchingForPdf = pdf;
		searchingForVideos = videos;
		searchingForPpt = ppt;
		searchingForEmbed = embed;
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
		crawlThePageForContent();
	}

	protected String[] documentType   = { ".doc",      ".docm",     ".docx"   };
	protected String[] powerPointType = { ".potx",     ".ppt",     ".pptx"   };

	public void crawlThePageForContent() {
//		System.out.println(getName() + " is Sniffing...");
		if (page == null) return;
		Elements links = page.getElementsByAttribute("href");
		links.stream().map(link -> link.attr("href")).forEach(this::applyFilter);
	}

	public HashMap<String, String> getFoundDocuments() {
		getPageContent();
		if (page == null) return null;
//		System.out.println(getName() + " is Sniffing...");
		Elements links = page.getElementsByAttribute("href");
		links.stream().map(link -> link.attr("href")).forEach(this::applyFilter);

		if (searchingForVideos) scrapeMediaAndYouTubeLinks(links);
		if (searchingForEmbed) scrapeForEmbeddedVideoLinks();
		return foundDocuments;
	}

	private void scrapeMediaAndYouTubeLinks(Elements links) {
		links.stream()
				.map(link -> link.attr("href"))
				.forEach(str -> {
					if (str.contains("youtube.com")) {
						if (str.replace("https://", "http://").replace("http://", "").toLowerCase().startsWith("youtube.com"))
							foundDocuments.put(str, "YouTube");
						else {
							String base = "http%3A%2F%2Fwww.youtube.com%2F";
							String videoKey = "watch%3Fv%3D";
							String channelKey = "user%2F";
							String temp = str.replace("https%3", "http%3");
							if (temp.contains(base)) {
								int index = temp.indexOf(base);
								String parsed = str.substring(index)
										.replace("%3A", ":")
										.replace("%2F", "/")
										.replace("%3D", "=")
										.replace("%3F", "?")
										.replace("&a=YouTube", "");
								if (str.contains(videoKey))
									foundDocuments.put(parsed, "YT-VID");
								if (temp.contains(channelKey) || temp.contains("c%2F"))
									foundDocuments.put(parsed, "YT-CHAN");

							}
						}
					}
					if (str.contains(".mp4")) {
//						System.out.println("Media File Found: " + str);
						foundDocuments.put(str, "MEDIA");
					}
				});
	}

	private void scrapeForEmbeddedVideoLinks() {
		Elements links = page.select("iframe");
		if (!links.isEmpty()) {
			links.forEach(element -> {
				String iframeSource = element.attr("src");
				if (iframeSource.contains("youtube.com")) {
//					System.out.println("iframe: -> " + element);
					foundDocuments.put(element.toString(), "EMBED");
				}
			});
		}
	}

	protected void applyFilter(String link) {
		if (searchingForPdf && link.endsWith(".pdf")) {
			foundDocuments.put(link, "PDF");
			return;
		}
		if (searchingForPpt) {
			if (Arrays.stream(powerPointType).anyMatch(link::endsWith)) {
				foundDocuments.put(link, "PPT");
			}
		}
		if (searchingForDocx) {
			if (Arrays.stream(documentType).anyMatch(link::endsWith)) {
				foundDocuments.put(link, "Document");
			}
		}
	}
}
