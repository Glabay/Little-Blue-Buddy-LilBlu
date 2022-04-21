package xyz.glabaystudios.web.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Getter;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.LilBlu;
import xyz.glabaystudios.web.model.whois.Whois;
import xyz.glabaystudios.web.model.whois.WhoisLookup;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static xyz.glabaystudios.web.gui.LilBluMainUI.CallType.INBOUND;
import static xyz.glabaystudios.web.gui.LilBluMainUI.CallType.OUTBOUND;

public class LilBluMainUI implements Initializable {

	public Font x1;
	public Pane dnsPane;
	public Rectangle dnsSquare;
	@FXML private TextField domainField;
	@FXML private Label callTypeLabel;
	@FXML private Label domainLabel;
	@FXML private Label familyLabel;
	@FXML private Label mxLabel;
	@FXML private Label domainNameServerLabel;
	@FXML private ListView<String> socialMediaListView = new ListView<>(FXCollections.observableArrayList("ns1", "ns2"));

	@Getter Whois result;

	private final Stop[] stops = new Stop[] {
			new Stop(0, Color.web("#EF7832")),
			new Stop(1, Color.web("#FFE6A7"))
	};

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		LinearGradient lngnt = new LinearGradient(1, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
		dnsSquare.setFill(lngnt);
	}

	public void resetEverything() {
		callTypeLabel.setTextFill(Paint.valueOf("BLACK"));
		familyLabel.setTextFill(Paint.valueOf("BLACK"));

		domainLabel.setText("DOMAIN NAME HERE");
		familyLabel.setText("IN-FAMILY OR NOT");
		mxLabel.setTextFill(Color.BLACK);
		mxLabel.setUnderline(false);
		mxLabel.setText("Has ## Mail Server(s)");
		callTypeLabel.setText("WAS THIS AN INBOUND CALL?");
		domainNameServerLabel.setText("Name Servers here");
		domainNameServerLabel.setTextFill(Color.BLACK);
		domainNameServerLabel.setUnderline(false);

		socialMediaListView.getItems().clear();
	}

	public void setCallInbound() {
		setCallType(INBOUND);
	}

	public void setCallOutbound() {
		setCallType(OUTBOUND);
	}

	public void setCallType(CallType callType) {
		if (callType.equals(INBOUND)) {
			callTypeLabel.setTextFill(Paint.valueOf("GREEN"));
			callTypeLabel.setText("~REMEMBER THE SURVEY~");
		}
		if (callType.equals(OUTBOUND)) {
			callTypeLabel.setTextFill(Color.web("#007C9B"));
			callTypeLabel.setText("~ YOU'VE GOT THIS ~");
		}
	}

	public void executeWhoisLookup() {
		String domain = domainField.getText()
				.replace("https://", "")
				.replace("http://", "")
				.replace("www.", "");
		if (domain.isEmpty()) return;
		Alert dnsInfo = new Alert(Alert.AlertType.INFORMATION);
		dnsInfo.initOwner(Controllers.getMainWindow().getScene().getWindow());
		WhoisLookup lookup = new WhoisLookup(domain.split("/")[0]);
		lookup.filterDumpedData();
		resetEverything();

		domainLabel.setText(lookup.getResult().getDomainName());
		if (lookup.getResult().isInFamily()) {
			familyLabel.setTextFill(lookup.getResult().isNewlyRegistered() ? Paint.valueOf("GREEN") : Paint.valueOf("PURPLE"));
			familyLabel.setText("IN-FAMILY" + (lookup.getResult().isNewlyRegistered() ? " NEW!" : ""));
		} else {
			familyLabel.setTextFill(Paint.valueOf("RED"));
			familyLabel.setText("NOT IN FAMILY");
		}
		mxLabel.setText(String.format("Has %d mail exchange server%s", lookup.getResult().getMailServerCount(), (lookup.getResult().getMailServerCount() > 1 ? "s" : "")));
		if (lookup.getResult().getMailServerCount() > 0) {
			mxLabel.setTextFill(Color.BLUE);
			mxLabel.setUnderline(true);
			mxLabel.setOnMouseClicked(mouseEvent -> {
				if (mxLabel.isUnderline()) {
					StringBuilder message = new StringBuilder();
					lookup.getResult().getMailServers().forEach(mailServer -> message.append(mailServer).append("\n"));
					dnsInfo.setTitle("Mail Exchange Servers.");
					dnsInfo.setHeaderText("Mail server for " + lookup.getResult().getDomainName());
					setMessageAndShow(dnsInfo, message);
				}
			});
		}

		if (!lookup.getResult().getSocialLinkMap().isEmpty()) {
			socialMediaListView.setCellFactory(TextFieldListCell.forListView());
			socialMediaListView.setEditable(false);
			result = lookup.getResult();
			int index = 0;
			for(String key : lookup.getResult().getSocialLinkMap().keySet()) {
				String entry = key + ": " + lookup.getResult().getSocialLinkMap().get(key).getSocialDisplayName();
				socialMediaListView.getItems().add(index++, entry);
			}
		}
		if (lookup.getResult().getNameServers().size() > 0) {
			String labelText = "There are %d Name Servers";
			domainNameServerLabel.setText(String.format(Locale.getDefault(), labelText, lookup.getResult().getNameServers().size()));
			domainNameServerLabel.setTextFill(Color.BLUE);
			domainNameServerLabel.setUnderline(true);
			domainNameServerLabel.setOnMouseClicked(mouseEvent -> {
				if (domainNameServerLabel.isUnderline()) {
					StringBuilder message = new StringBuilder();
					lookup.getResult().getNameServers().forEach(nameServer -> message.append(nameServer).append("\n"));
					dnsInfo.setTitle("Name Server List.");
					dnsInfo.setHeaderText("Name Servers for " + lookup.getResult().getDomainName());
					setMessageAndShow(dnsInfo, message);
				}
			});
		}

	}

	private void setMessageAndShow(Alert dnsInfo, StringBuilder message) {
		dnsInfo.setContentText(message.toString());
		dnsInfo.show();
	}

	public void requestCloseAction() {
		System.exit(0);
	}

	public void requestTemplateWindowException() {
		Scene scene = setupTheScene(Controllers.getCiscoCodeExceptionWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "Exception E-mailer");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeCiscoCodeExceptionWindow());
			stage.show();
		}
	}

	public void requestTemplateWindowFollowUp() {
		Scene scene = setupTheScene(Controllers.getFollowUpWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "Follow up Template");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeFollowUpWindow());
			stage.show();
		}
	}

	public void requestTemplateWindowMissedEvent() {
		Scene scene = setupTheScene(Controllers.getMissedEventWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "Missed/Late Event");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeMissedEventWindow());
			stage.show();
		}
	}

	public void requestTemplateWindowScheduleCallback() {
		Scene scene = setupTheScene(Controllers.getCallbackWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "Scheduled Callback");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeCallbackWindow());
			stage.show();
		}
	}

	public void requestAboutWindow() {
		LilBlu.openAbout();
	}

	public void executeMouseActionOnSocialLink(MouseEvent mouseEvent) {
		String selectedItem = socialMediaListView.getSelectionModel().getSelectedItem();
		String socialPlatform = selectedItem.split(":")[0];
		String url = getResult().getSocialLinkMap().get(selectedItem.split(":")[0]).getSocialPlatformURL();
		String button = mouseEvent.getButton().name();
		if (button.equals("PRIMARY"))
			Toolkit.getDefaultToolkit()
					.getSystemClipboard()
					.setContents(
							new StringSelection(socialPlatform + ":\n" + url + "\n"), null);

		if (button.equals("SECONDARY")) openWebpage(URI.create(url));
	}

	public void loadDocumentRenamer() {
		Scene scene = setupTheScene(Controllers.getDocumentRenamingWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "Document Renaming");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeDocumentRenamingWindow());
			stage.show();
		}
	}

	public void prepTheHounds() {
		Scene scene = setupTheScene(Controllers.getDocuHoundWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "DocuHound");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeDocuHoundWindow());
			stage.show();
		}
	}

	public void openEcommRipper() {
		Scene scene = setupTheScene(Controllers.getEcommRipperWindow());
		if (scene != null) {
			Stage stage = setTheStage(scene, "Store Ripper");
			stage.setOnCloseRequest(windowEvent -> Controllers.removeEcommRipper());
			stage.show();
		}
	}

	private Scene setupTheScene(Parent parent) {
		Scene scene = null;
		try {
			scene = new Scene(parent);
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: setupTheScene ->" + parent.toString(), e);
		}
		return scene;
	}

	private Stage setTheStage(Scene scene, String windowTitle) {
		Stage stage = new Stage();
		stage.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
		stage.setResizable(false);
		stage.setAlwaysOnTop(true);
		stage.setTitle(windowTitle);
		stage.setScene(scene);
		stage.setOnShown(windowEvent -> stage.toFront());
		return stage;
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("openWebpage", e);
			}
		}
	}

	public void handleDnsLookup(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.ENTER && domainField.getText().length() > 3) executeWhoisLookup();
	}

	enum CallType {
		INBOUND, OUTBOUND
	}
}
