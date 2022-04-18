package xyz.glabaystudios.web.gui.pound;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
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

	ExecutorService executorService;
	Map<String, String> foundDocuments = new HashMap<>();


	public void prepareThePackForTheHunt() {
		Alert domainAlert = new Alert(AlertType.WARNING,  "You must provide a place for the hounds to search<br>Please provide a Domain.", ButtonType.CLOSE);
		Alert unknownDocument = new Alert(AlertType.WARNING,  "What documents are you hunting?.", ButtonType.CLOSE);

		domainAlert.initOwner(Controllers.getDocuHoundWindow().getScene().getWindow());
		unknownDocument.initOwner(Controllers.getDocuHoundWindow().getScene().getWindow());

		if (domainField.getText().isEmpty()) {
			domainAlert.show();
			return;
		}
		String domain = getFormattedDomain(domainField.getText());
		if (!docxChk.isSelected() && !pptChk.isSelected() && !pdfChk.isSelected() && !videoChk.isSelected()) {
			unknownDocument.show();
			return;
		}
		packStatusLabel.setText("");
		String sitemap = getSitemapPage(domain, false);
		String target = domain + sitemap;

		packStatusLabel.setText("Waking the pack leader...");
		PackLeader docuHoundPack = new PackLeader(domain);
		docuHoundPack.setName("Glabay-Studios-LilBlu-DocuHound");
		docuHoundPack.setTarget(target);
		docuHoundPack.setTargetDocuments(docxChk.isSelected(), pdfChk.isSelected(), videoChk.isSelected(), pptChk.isSelected(), embedChk.isSelected());
		executorService = Executors.newSingleThreadExecutor();
		packStatusLabel.setText("Preparing the pack.");
		Future<HashMap<String, String>> docuHoundLeader = executorService.submit(docuHoundPack::getFoundDocuments);
//		relTheHndBtn.setDisable(true);
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
				executorService.shutdown();
			}
		}
		updateFoundList();
	}

	private String getSitemapPage(String domain, boolean fallback) {
		String result = "/sitemap.xml";
		try {
			URL domainSitemap = new URL(domain + (fallback ? "" : "/robots.txt"));

			HttpURLConnection connection = (HttpURLConnection) domainSitemap.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			connection.setInstanceFollowRedirects(true);
			int code = connection.getResponseCode();
			System.out.println("Code: " + code);
			boolean redirect = false;
			if (code != HttpURLConnection.HTTP_OK) {
				if (code == HttpURLConnection.HTTP_MOVED_TEMP
						|| code == HttpURLConnection.HTTP_MOVED_PERM
						|| code == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}
			if (redirect) {
				result = getSitemapPage(connection.getHeaderField("Location"), true);
				System.out.println("Rerouting  <-|-> " + result);
				return result;
			} else {
				InputStreamReader inputStreamReader = new InputStreamReader(domainSitemap.openStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String inputLine;
				while ((inputLine = bufferedReader.readLine()) != null) {
					if (inputLine.startsWith("Sitemap:")) {
						System.out.println("AltMap-Found: <-|-> " + inputLine);
					}
					if (inputLine.startsWith("Disallow:")) {
						System.out.println(inputLine);
					}
				}
				bufferedReader.close();
			}
		} catch (MalformedURLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> MalformedURL", e);
		} catch (SSLException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> SSLException\nChecking again without SSL", e);
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("connectAndFetchSitemap -> InputOutput", e);
		}

		return result;
	}

	private void updateFoundList() {
		packStatusLabel.setText("Pack is returning...");
		System.out.println("Done!");

		foundDocumentList.getItems().clear();
		foundPowerPointList.getItems().clear();
		foundEmbeddedList.getItems().clear();
		foundPdfList.getItems().clear();
		foundVideoList.getItems().clear();
		foundAdditionalLinksList.getItems().clear();


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
