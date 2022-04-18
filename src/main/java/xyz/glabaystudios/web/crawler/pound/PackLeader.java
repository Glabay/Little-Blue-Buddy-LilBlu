package xyz.glabaystudios.web.crawler.pound;

import org.jsoup.select.Elements;
import xyz.glabaystudios.net.NetworkExceptionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PackLeader extends DocumentHound {

	List<DocumentHound> pack;
	List<String> pagesToSniff;

	ExecutorService executorService = Executors.newCachedThreadPool();

	HashMap<String, String> finalCopyOfLinks = new HashMap<>();

	public PackLeader(String domain) {
		super(domain);
		pagesToSniff = new ArrayList<>();
	}

	private void connectAndFetchSitemap(String domainsSitemap) {
		System.out.println("Looking for the sitemap");
		String domain = domainPage
				.toLowerCase()
				.replace("http://", "")
				.replace("https://", "");

		Elements locations = getContent(domain, userAgent, false).select("loc");
		for (org.jsoup.nodes.Element loc : locations) {
			String pageLink = loc.text();
			if (!filteredOut(pageLink)) pagesToSniff.add(pageLink);
		}
	}

	protected boolean filteredOut(String link) {
		if (searchingForPdf && link.endsWith(".pdf")) {
			foundDocuments.put(link, "PDF");
			return true;
		}
		if (searchingForPpt) {
			for (String ext : powerPointType) {
				if (link.endsWith(ext)) {
					foundDocuments.put(link, "PPT");
					return true;
				}
			}
		}
		if (searchingForDocx) {
			for (String ext : documentType) {
				if (link.endsWith(ext)) {
					foundDocuments.put(link, "Document");
					return true;
				}
			}
		}
		return false;
	}

	private void assignHounds() {
		for(String link : pagesToSniff) {
			DocumentHound docuHoundPack = new DocumentHound(domainHome);
			docuHoundPack.setTarget(link);
			docuHoundPack.setTargetDocuments(searchingForDocx, searchingForPdf, searchingForVideos, searchingForPpt, searchingForEmbed);
			docuHoundPack.setName("Glabay-Studios-LilBlu-DocuHound-" + link.replace(domainHome, ""));
			pack.add(docuHoundPack);
		}
	}

	public HashMap<String, String> getFoundDocuments() {
		connectAndFetchSitemap(domainPage);
		pack = new ArrayList<>(pagesToSniff.size());
		System.out.println("Calling for a pack of: " + pagesToSniff.size());

		// pop up to confirm that there are X pages going to be crawled for documents ^ video links/embedded iframes
		assignHounds();
		System.out.println("Pack is assigned pages.");

		// pop up for the agent to confirm with the customer that they are allowing us to crawl their domain for documents ^ video links/embedded iframes
		// and that this may take a moment
		releaseThePack(pack.stream().<Callable<HashMap<String, String>>>map(hound -> hound::getFoundDocuments).collect(Collectors.toList()));
		return finalCopyOfLinks;
	}

	private void releaseThePack(Collection<Callable<HashMap<String, String>>> callables) {
		System.out.printf("Pack Ready! size: %s%nReleasing the Hounds!%n", callables.size());
		while (!executorService.isShutdown()) {
			try {
				List<Future<HashMap<String, String>>> taskFutureList = executorService.invokeAll(callables);
				for (Future<HashMap<String, String>> future : taskFutureList) {
					HashMap<String, String> value = future.get(30, TimeUnit.SECONDS);
					if (value != null) finalCopyOfLinks.putAll(value);
				}
			} catch (InterruptedException e) {
				NetworkExceptionHandler.handleException("getFoundDocuments -> Interrupted", e);
			} catch (ExecutionException e) {
				NetworkExceptionHandler.handleException("getFoundDocuments -> Execution", e);
			} catch (TimeoutException e) {
				NetworkExceptionHandler.handleException("getFoundDocuments -> InputOutput", e);
			} finally {
				executorService.shutdown();
				if (finalCopyOfLinks.size() > 0) System.out.println("Successful hunt!");
			}
		}
	}
}
