package xyz.glabaystudios.web.crawler.pound;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import xyz.glabaystudios.net.NetworkExceptionHandler;

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

	StringBuilder sitemap;

	ExecutorService executorService = Executors.newCachedThreadPool();

	HashMap<String, String> finalCopyOfLinks = new HashMap<>();

	public PackLeader(String domain) {
		super(domain);
		sitemap = new StringBuilder();
	}


	private void connectAndFetchSitemap() {
		try {
			URL domainSitemap = new URL((domainHome + domainPage));
			InputStreamReader inputStreamReader = new InputStreamReader(domainSitemap.openStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String inputLine;
			while((inputLine = bufferedReader.readLine()) != null) sitemap.append(inputLine);
			bufferedReader.close();
		} catch (MalformedURLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> MalformedURL", e);
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> InputOutput", e);
		}
	}

	void openConnectionAndReadSitemap() {
		System.out.println("Looking for the sitemap");
		try {
			pagesToSniff = new ArrayList<>();
			connectAndFetchSitemap();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource pageSource = new InputSource();
			pageSource.setCharacterStream(new StringReader(sitemap.toString()));

			Document doc = db.parse(pageSource);
			NodeList nodes = doc.getElementsByTagName("url");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				NodeList name = element.getElementsByTagName("loc");
				Element line = (Element) name.item(0);
				String pageLink = getCharacterDataFromElement(line);
				System.out.printf("Found page: %s%n", pageLink);
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
//			System.out.println("assigning pages");
			String pageName = link.substring(domainHome.length());
			DocumentHound docuHoundPack = new DocumentHound(domainHome);
			docuHoundPack.setTarget(pageName);
			docuHoundPack.setTargetDocuments(searchingForDocx, searchingForPdf, searchingForVideos, searchingForPpt);
//			System.out.println("Hound assigned: " + pageName);
			docuHoundPack.setName("Glabay-Studios-LilBlu-DocuHound-" + pageName);
			pack.add(docuHoundPack);
		}
	}

	public HashMap<String, String> getFoundDocuments() {
		openConnectionAndReadSitemap();
		System.out.println("Calling for a pack of: " + pagesToSniff.size());
		pack = new ArrayList<>(pagesToSniff.size());

		assignHounds();
		System.out.println("Pack is ready to run!\nRelease the Hounds!");
		Collection<Callable<HashMap<String, String>>> callables = pack.stream().<Callable<HashMap<String, String>>>map(hound -> hound::getFoundDocuments).collect(Collectors.toList());
		while(!executorService.isShutdown()) {
			try {
				List<Future<HashMap<String, String>>> taskFutureList = executorService.invokeAll(callables);
				System.out.println("Preparing the pack of: " + taskFutureList.size());
				for (Future<HashMap<String, String>> future : taskFutureList) {
					/* get Double result from Future when it becomes available */
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
			}
		}
		System.out.println("Successful hunt!");
		return finalCopyOfLinks;
	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}
}
