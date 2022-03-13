package xyz.glabaystudios.web.crawler.pound;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import xyz.glabaystudios.net.NetworkExceptionHandler;

import javax.net.ssl.SSLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PackLeader extends DocumentHound {

	List<DocumentHound> pack;
	List<String> pagesToSniff;
	List<String> additionalMaps;

	private boolean sitemapFound = false;

	ExecutorService executorService = Executors.newCachedThreadPool();

	HashMap<String, String> finalCopyOfLinks = new HashMap<>();

	public PackLeader(String domain) {
		super(domain);
		pagesToSniff = new ArrayList<>();
	}

	private void connectAndFetchSitemap(String domainsSitemap) {
		StringBuilder sitemap = new StringBuilder();
		try {
			URL domainSitemap = new URL(domainsSitemap);
			InputStreamReader inputStreamReader = new InputStreamReader(domainSitemap.openStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String inputLine;
			while((inputLine = bufferedReader.readLine()) != null) sitemap.append(inputLine);
			bufferedReader.close();
		} catch (MalformedURLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> MalformedURL", e);
		} catch (SSLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> SSLException\nChecking again without SSL", e);
			connectAndFetchSitemap(domainsSitemap.replace("https://", "http://"));
			return;
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> InputOutput", e);
		}
		applySubSitemapCheck(sitemap.toString());
	}

	private void applySubSitemapCheck(String sitemap) {
		System.out.println("Looking for the additional sitemaps...");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource pageSource = new InputSource();
			pageSource.setCharacterStream(new StringReader(sitemap));

			try {
				Document doc = db.parse(pageSource);
				NodeList nodes = doc.getElementsByTagName("sitemap");
				if (nodes != null && nodes.getLength() > 0) {
				System.out.println("Found additional sitemap...");
				additionalMaps = new ArrayList<>();
				for (int i = 0; i < nodes.getLength(); i++) {
					Element element = (Element) nodes.item(i);
					NodeList name = element.getElementsByTagName("loc");
					Element line = (Element) name.item(0);
					String pageLink = getCharacterDataFromElement(line);
					System.out.println("adding additional sitemap to be indexed after this one...");
					additionalMaps.add(pageLink);
				}
				additionalMaps.forEach(this::connectAndFetchSitemap);
			} else {
				System.out.println("Just the single sitemap, moving on...");
				openConnectionAndReadSitemap(sitemap);
			}
			} catch (SAXException e) {
				NetworkExceptionHandler.handleException("applySubSitemapCheck -> SAX ", e);
				sitemapFound = false;
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("applySubSitemapCheck -> InputOutput ", e);
			}
		} catch (ParserConfigurationException e) {
			NetworkExceptionHandler.handleException("applySubSitemapCheck -> ParserConfiguration ", e);
		}
	}

	void openConnectionAndReadSitemap(String sitemap) {
//		System.out.println("Reading map: " + sitemap);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource pageSource = new InputSource();
			pageSource.setCharacterStream(new StringReader(sitemap));

//			System.out.println("Reading: " + pageSource);
			Document doc = db.parse(pageSource);
			NodeList nodes = doc.getElementsByTagName("url");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				NodeList name = element.getElementsByTagName("loc");
				Element line = (Element) name.item(0);
				String pageLink = getCharacterDataFromElement(line);
//				System.out.printf("Found page: %s%n", pageLink);
				if (!filteredOut(pageLink)) pagesToSniff.add(pageLink);
			}
		} catch (SAXException e) {
			NetworkExceptionHandler.handleException("openConnectionAndReadSitemap -> SAX", e);
		} catch (ParserConfigurationException e) {
			NetworkExceptionHandler.handleException("openConnectionAndReadSitemap -> ParserConfiguration", e);
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("openConnectionAndReadSitemap -> InputOutput", e);
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
			String pageName = link.substring(domainHome.length());
			DocumentHound docuHoundPack = new DocumentHound(domainHome);
			docuHoundPack.setTarget(link);
			docuHoundPack.setTargetDocuments(searchingForDocx, searchingForPdf, searchingForVideos, searchingForPpt);
			docuHoundPack.setName("Glabay-Studios-LilBlu-DocuHound-" + pageName);
			pack.add(docuHoundPack);
		}
	}

	public HashMap<String, String> getFoundDocuments() {
		System.out.println("Looking for the sitemap");
		connectAndFetchSitemap(domainPage);
		System.out.println("Calling for a pack of: " + pagesToSniff.size());
		pack = new ArrayList<>(pagesToSniff.size());

		assignHounds();

		System.out.println("Pack is assigned pages.");
		releaseThePack(pack.stream().<Callable<HashMap<String, String>>>map(hound -> hound::getFoundDocuments).collect(Collectors.toList()));
		return finalCopyOfLinks;
	}

	private void releaseThePack(Collection<Callable<HashMap<String, String>>> callables) {
		System.out.printf("Preparing the pack of: %s%nReleasing the Hounds!%n", callables.size());
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

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}

	public List<DocumentHound> getPack() {
		return pack;
	}
}
