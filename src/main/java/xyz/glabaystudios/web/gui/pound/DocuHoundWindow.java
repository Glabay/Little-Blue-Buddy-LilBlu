package xyz.glabaystudios.web.gui.pound;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.crawler.pound.PackLeader;

import javax.net.ssl.SSLException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DocuHoundWindow {

	public Label packStatusLabel = new Label();
	public Label foundFilesWord;
	public Label foundFilesVideo;
	public Label foundFilesEmbed;
	public Label foundFilesPdf;
	public Label foundFilesPpt;
	public ListView<String> foundDocumentList = new ListView<>(FXCollections.observableArrayList("DOCX", "docx"));
	public ListView<String> foundPdfList = new ListView<>(FXCollections.observableArrayList("PDF", "pdf"));
	public ListView<String> foundPowerPointList = new ListView<>(FXCollections.observableArrayList("PPT", "ppt"));
	public ListView<String> foundVideoList = new ListView<>(FXCollections.observableArrayList("VID", "mp4"));
	public ListView<String> foundEmbeddedList = new ListView<>(FXCollections.observableArrayList("EMBED", "iframe"));
	public ListView<String> foundAdditionalLinksList = new ListView<>(FXCollections.observableArrayList("LINKS", "uri"));
	public Button saveDocsBtn;
	public CheckBox videoChk;
	public CheckBox embedChk;
	public CheckBox pptChk;
	public CheckBox pdfChk;
	public CheckBox docxChk;
	public Button discardExpBtn;
	public Button relTheHndBtn;
	public TextField domainField;

	private Map<String, String> foundDocuments = new HashMap<>();


	public void prepareThePackForTheHunt() {
		Alert domainAlert = new Alert(AlertType.WARNING, "Warning", ButtonType.CLOSE);

		domainAlert.initOwner(Controllers.getDocuHoundWindow().getScene().getWindow());

		if (domainField.getText().isEmpty()) {
			domainAlert.setTitle("Missing Domain");
			domainAlert.setContentText("You must provide a place for the hounds to search<br>Please provide a Domain.");
			domainAlert.show();
			return;
		}
		String domain = getFormattedDomain(domainField.getText());

		if (!docxChk.isSelected() && !pptChk.isSelected() && !pdfChk.isSelected() && !videoChk.isSelected() && !embedChk.isSelected()) {
			domainAlert.setTitle("Unknown Request");
			domainAlert.setContentText("What documents are you hunting for?");
			domainAlert.show();
			return;
		}
		reset();
		packStatusLabel.setText("");
		String sitemap = getSitemapPage(domain, false);
		String target = domain + sitemap;
		List<String> makeshiftSitemap = null;
		if (sitemap == null) {
			target = domain;
			makeshiftSitemap = buildMakeshiftSitemap(domain);
		}
		releaseTheLeader(domain, target, (sitemap == null), makeshiftSitemap);
	}

	/**
	 * Looking at the home page, we will look over all href's and then filter over them to make sure they are for this domain
	 * @param domain the domain to search
	 * @return A List of page links to assign to the pack
	 */
	private List<String> buildMakeshiftSitemap(String domain) {
		List<String> makeshiftSitemap = new ArrayList<>();
		System.out.println("well, no robots guidance, no map, no light, no problem... I think...");
		System.out.println("Let's see here what we find, time to start an adventure...");
		String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";
			try {
				Document homepage = Jsoup.connect(domain).userAgent(userAgent).timeout(30000).ignoreContentType(true).ignoreHttpErrors(true).get();
				Elements elements = homepage.getElementsByAttribute("href");
				elements.stream()
						.filter(ele -> !ele.text().isEmpty())
						.map(ele -> ele.attr("href"))
						.filter(pageLink -> !pageLink.toLowerCase().startsWith("http"))
						.filter(pageLink -> !pageLink.toLowerCase().startsWith("www."))
						.forEach(pageLink -> {
							String link = (domain.endsWith("/") ? domain : (domain + "/"));
							System.out.println(link + pageLink);
							makeshiftSitemap.add(link + pageLink);
						});
				System.out.println("Found pages: " + makeshiftSitemap.size());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		return makeshiftSitemap;
	}

	private void releaseTheLeader(String domain, String target, boolean usingMakeshiftMap, List<String> makeshiftSitemap) {
		packStatusLabel.setText("Waking the pack leader...");
		PackLeader docuHoundPack = new PackLeader(domain);
		docuHoundPack.setName("Glabay-Studios-LilBlu-DocuHound");
		if (usingMakeshiftMap) {
			docuHoundPack.setTarget(null);
			docuHoundPack.setMakeshiftSitemap(makeshiftSitemap);
		} else docuHoundPack.setTarget(target);

		docuHoundPack.setTargetDocuments(docxChk.isSelected(), pdfChk.isSelected(), videoChk.isSelected(), pptChk.isSelected(), embedChk.isSelected());
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<HashMap<String, String>> docuHoundLeader = executorService.submit(docuHoundPack::getFoundDocuments);
//		        relTheHndBtn.setDisable(true); // TODO: Uncomment before production
		packStatusLabel.setText("The pack is out hunting for documents...");
		System.out.println("Checking in on the pack");
		while(!executorService.isShutdown()) {
			try {
				foundDocuments = docuHoundLeader.get(90, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				NetworkExceptionHandler.handleException("prepareThePackForTheHunt -> Interrupted " + e.getCause(), e);
			} catch (ExecutionException e) {
				NetworkExceptionHandler.handleException("prepareThePackForTheHunt -> Execution " + e.getCause(), e);
			} catch (TimeoutException e) {
				NetworkExceptionHandler.handleException("prepareThePackForTheHunt -> Timeout " + e.getCause(), e);
			} finally {
				packStatusLabel.setText("Pack is returning...");
				executorService.shutdown();
			}
		}
		updateFoundList();
	}

	/**
	 * A quick reset of the fields and list views
	 */
	private void reset() {
		foundDocumentList.getItems().clear();
		foundPowerPointList.getItems().clear();
		foundEmbeddedList.getItems().clear();
		foundPdfList.getItems().clear();
		foundVideoList.getItems().clear();
		foundAdditionalLinksList.getItems().clear();

		foundFilesWord.setText("Total amount of Word files: 0");
		foundFilesVideo.setText("Total amount of Video files: 0");
		foundFilesEmbed.setText("Total Embedded links found: 0");
		foundFilesPdf.setText("Total amount of PDF files: 0");
		foundFilesPpt.setText("Total PowerPoint files: 0");

	}

	/**
	 * Looking for a sitemap
	 * <p>Looking first for a /robots.txt we will see if there is a sitemap listed
	 * If there is no Robots.txt then we will just fallback to the domain/sitemap.xml<br>
	 * From here we will also check for Redirects, and follow the rabbit to wonderland<br>
	 * If we don't have a Sitemap.xml then we return null to trigger a call to DocuHoundWindow.buildMakeshiftSitemap()
	 * </p>
	 * @param domain the domain to check
	 * @param fallback if there is no robots.txt we will fallback to sitemap.xml
	 * @return a link to the sitemap or null if nothing is found.
	 */
	private String getSitemapPage(String domain, boolean fallback) {
		String result = "/sitemap.xml";
		try {
			URL domainSitemap = new URL(domain + (fallback ? "" : "/robots.txt"));

			HttpURLConnection connection = (HttpURLConnection) domainSitemap.openConnection();
			connection.setRequestMethod("GET");
			System.out.println(connection.getURL().toString());
			connection.connect();
			connection.setInstanceFollowRedirects(true);
			int code = connection.getResponseCode();
			System.out.println("Code: " + code);
			if (code == 404) return null;
			if (code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_SEE_OTHER)
				return getSitemapPage(connection.getHeaderField("Location"), true);
			InputStreamReader inputStreamReader = new InputStreamReader(domainSitemap.openStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String inputLine;
			boolean foundMap = false;
			while ((inputLine = bufferedReader.readLine()) != null) {
				if (inputLine.startsWith("Sitemap:")) {
					System.out.println("AltMap-Found: <-|-> " + inputLine);
					result =  inputLine.split("Sitemap:")[1].replace((fallback ? (domain.replace("/robots.txt", "")) : domain), "").trim();
					System.out.println("AltMap: <-|-> " + result);
					foundMap = true;
					break;
				}
//				if (inputLine.startsWith("Disallow:")) System.out.println(inputLine);
			}
			bufferedReader.close();
			System.out.println("There was " + (foundMap ? "a" : "no") + " sitemap found" + (foundMap ? (": " + result) : "") );
			if (!foundMap) {
				return getSitemapPage(domain + "/sitemap.xml", true);
			}
		} catch (MalformedURLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> MalformedURL", e);
		} catch (SSLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> SSLException", e);
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> InputOutput", e);
		}

		return result;
	}

	private void updateFoundList() {
		System.out.println("Done!");

		reset();

		System.out.println("Collected: " + foundDocuments.size() + " documents");
		for (String link : foundDocuments.keySet()) {
			String key = foundDocuments.get(link);
			switch(key) {
				case "Document":
					foundDocumentList.getItems().add(foundDocumentList.getItems().size(), key + " | " + link);
					break;
				case "PDF":
					foundPdfList.getItems().add(foundPdfList.getItems().size(), key + " | " + link);
					break;
				case "PPT":
					foundPowerPointList.getItems().add(foundPowerPointList.getItems().size(), key + " | " + link);
					break;
				case "MEDIA":
					foundVideoList.getItems().add(foundVideoList.getItems().size(), key + " | " + link);
					break;
				case "EMBED":
					foundEmbeddedList.getItems().add(foundEmbeddedList.getItems().size(), key + " | " + link);
					break;
				default:
					foundAdditionalLinksList.getItems().add(foundAdditionalLinksList.getItems().size(), key + " | " + link);
					break;
			}
		}
		foundFilesWord.setText("Total amount of Word files: " + foundDocumentList.getItems().size());
		foundFilesVideo.setText("Total amount of Video files: " + foundVideoList.getItems().size());
		foundFilesEmbed.setText("Total Embedded links found: " + foundEmbeddedList.getItems().size());
		foundFilesPdf.setText("Total amount of PDF files: " + foundPdfList.getItems().size());
		foundFilesPpt.setText("Total PowerPoint files: " + foundPowerPointList.getItems().size());

		System.out.println("Total additional files: " + foundAdditionalLinksList.getItems().size());
		foundAdditionalLinksList.getItems().forEach(System.out::println);

		packStatusLabel.setText("Found " + foundDocuments.size() + " documents!");
		saveDocsBtn.setDisable(false);
	}

	private String getFormattedDomain(String passedDomain) {
		String cleaned = passedDomain
				.replace("http://", "")
				.replace("https://", "")
				.replace("www.", "");
		return "http://" + cleaned;
	}

	public void sendCloseAction() {
		Controllers.getDocuHoundWindow().getScene().getWindow().hide();
		Controllers.removeDocuHoundWindow();
	}

	/**
	 * Saving the Documents<br>
	 * <p>
	 *     Looking at the size of the documents the pack found, for every 16 documents<br>
	 *     we will have a hound handle fetching these 16, and if there happens to be anything left over,<br>
	 *     we will send out one more hound to fetch the straggler
	 * </p>
	 * Once the pack has been assigned their documents and gone to fetch, we will open the Downloads' directory if it is not opened already.
	 */
	public void saveDocuments() {
		System.out.println("Time to Save the Documents...");
		saveDocsBtn.setDisable(true);
		Downloader docuDownloader;
		int split = 16;
		int houndsNeeded = foundDocuments.size() / split;
		if (houndsNeeded >= 2) {
			int scraps = foundDocuments.size() % split;
			if (scraps > 0) houndsNeeded += 1;
			List<String> links = new ArrayList<>(foundDocuments.keySet());
			int linkIndex = 0;
			for (int index = 0; index < houndsNeeded; index++) {
				HashMap<String, String> tempDocs = new HashMap<>();
				int passes = Math.min(split, links.size()-1);
				for (int i = 0; i < passes; i++) {
					if (linkIndex == links.size()) linkIndex-=1;
					tempDocs.put(links.get(linkIndex), "LilBluDoc-"+i);
					linkIndex++;
					if (linkIndex > links.size()) break;
				}
				docuDownloader = new Downloader();
				docuDownloader.setDomain(getFormattedDomain(domainField.getText()));
				docuDownloader.setName("Glabay-Studios-LilBlu-DocuHound-Downloader#" + index);
				docuDownloader.passDocumentList(tempDocs);
				docuDownloader.start();
			}
		} else {
			docuDownloader = new Downloader();
			docuDownloader.setDomain(getFormattedDomain(domainField.getText()));
			docuDownloader.setName("Glabay-Studios-LilBlu-DocuHound-Downloader");
			docuDownloader.passDocumentList((HashMap<String, String>) foundDocuments);
			docuDownloader.start();
		}
		try {
			String downloadDir = (System.getProperty("user.home") + "/Downloads/");
			Desktop desktop = Desktop.getDesktop();
			desktop.open(new File(downloadDir));
			System.out.println("opening downloads dir...");
		} catch (IOException ignore) {
			System.out.println("Failed opening downloads dir...");
		}
	}
}
