package xyz.glabaystudios.web.gui.pound;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.crawler.pound.PackLeader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DocuHoundWindow {

	public Label packStatusLabel;
	public ListView<String> foundDocumentList = new ListView<>(FXCollections.observableArrayList("docx", "pdf"));
	public Button saveDocsBtn;
	public CheckBox videoChk;
	public CheckBox pptChk;
	public CheckBox pdfChk;
	public CheckBox docxChk;
	public Button discardExpBtn;
	public Button relTheHndBtn;
	public TextField domainField;

	ExecutorService executorService = Executors.newCachedThreadPool();
	Map<String, String> foundDocuments = new HashMap<>();

	Alert domainAlert = new Alert(AlertType.WARNING,  "You must provide a place for the hounds to search<br>Please provide a Domain.", ButtonType.CLOSE, ButtonType.OK);
	Alert unknownDocument = new Alert(AlertType.CONFIRMATION,  "What documents are you hunting?.", ButtonType.CLOSE, ButtonType.OK);

	public void prepareThePackForTheHunt() {
		if (domainField.getText().isEmpty()) {
			domainAlert.show();
			return;
		}
		String domain = getFormattedDomain(domainField.getText());
		if (!docxChk.isSelected() && !pptChk.isSelected() && !pdfChk.isSelected() && !videoChk.isSelected()) {
			unknownDocument.show();
			return;
		}
		String sitemap = "/wp-sitemap.xml";
		URL url;
		try {
			url = new URL((domain + sitemap));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			int code = connection.getResponseCode();
			System.out.println("Code: " + code);
			if (code == 404) sitemap = "/sitemap.xml";
		} catch (IOException e) {
			e.printStackTrace();
		}

		PackLeader docuHoundPack = new PackLeader(domain);
		docuHoundPack.setName("Glabay-Studios-LilBlu-DocuHound");
		docuHoundPack.setTarget(sitemap);
		docuHoundPack.setTargetDocuments(docxChk.isSelected(), pdfChk.isSelected(), videoChk.isSelected(), pptChk.isSelected());

		Future<HashMap<String, String>> docuHoundLeader = executorService.submit(docuHoundPack::getFoundDocuments);
		relTheHndBtn.setDisable(true);
		while(!executorService.isShutdown()) {
			try {
				foundDocuments = docuHoundLeader.get(42, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			} finally {
				updateFoundList();
				executorService.shutdown();
			}
		}
	}

	private void updateFoundList() {
		System.out.println("Done!");
		System.out.println("Collected: " + foundDocuments.size() + " documents!");
		int index = 0;
		for (String link : foundDocuments.keySet()) {
			String docType = foundDocuments.get(link);
			foundDocumentList.getItems().add(index++, docType + " | " + link);
		}
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
	}
}
