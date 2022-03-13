package xyz.glabaystudios.web.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Getter;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.LilBlu;
import xyz.glabaystudios.web.model.Whois;
import xyz.glabaystudios.web.model.WhoisLookup;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import static xyz.glabaystudios.web.gui.FxPanel.CallType.INBOUND;
import static xyz.glabaystudios.web.gui.FxPanel.CallType.OUTBOUND;

public class FxPanel {

	public Font x1;
	@FXML private TextField domainField;
	@FXML private Label callTypeLabel;
	@FXML private Label domainLabel;
	@FXML private Label familyLabel;
	@FXML private Label mxLabel;
	@FXML private Label domainNameServerLabel;
	@FXML private ListView<String> socialMediaListView = new ListView<>(FXCollections.observableArrayList("ns1", "ns2"));

	@Getter Whois result;

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
			// set color to GREEN
			callTypeLabel.setTextFill(Paint.valueOf("GREEN"));
			callTypeLabel.setText("~REMEMBER THE SURVEY AT THE EOC~");
		}
		if (callType.equals(OUTBOUND)) {
			// set color to GREEN
			callTypeLabel.setTextFill(Paint.valueOf("ORANGE"));
			callTypeLabel.setText("~ YOU GOT THIS ~");
		}
	}

	public void executeWhoisLookup() {
		String domain = domainField.getText();
		if (domain.isEmpty()) return;
		WhoisLookup lookup = new WhoisLookup(domain);
		lookup.filterDumpedData();
		domainField.clear();
		socialMediaListView.getItems().clear();

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
					Alert mailServerAlert = new Alert(Alert.AlertType.INFORMATION);
					StringBuilder message = new StringBuilder();
					lookup.getResult().getMailServers().forEach(mailServer -> message.append(mailServer).append("\n"));
					mailServerAlert.setTitle("Mail Exchange Servers.");
					mailServerAlert.setHeaderText("Mail server for " + lookup.getResult().getDomainName());
					setMessageAndShow(mailServerAlert, message);
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
					Alert nameServerAlert = new Alert(Alert.AlertType.INFORMATION);
					StringBuilder message = new StringBuilder();
					lookup.getResult().getNameServers().forEach(nameServer -> message.append(nameServer).append("\n"));
					nameServerAlert.setTitle("Name Server List.");
					nameServerAlert.setHeaderText("Name Servers for " + lookup.getResult().getDomainName());
					setMessageAndShow(nameServerAlert, message);
				}
			});
		}

	}

	private void setMessageAndShow(Alert nameServerAlert, StringBuilder message) {
		nameServerAlert.setContentText(message.toString());
		nameServerAlert.setOnHidden(window -> {
			Stage stage = (Stage) Controllers.getMainWindow().getScene().getWindow();
			stage.setAlwaysOnTop(true);
		});
		nameServerAlert.setOnShown(window -> {
			Stage stage = (Stage) Controllers.getMainWindow().getScene().getWindow();
			stage.setAlwaysOnTop(false);
		});
		nameServerAlert.showAndWait();
	}

	public void requestCloseAction() {
		System.exit(0);
	}

	public void requestTemplateWindowException() {
		Scene popup = null;
		try {
			popup = new Scene(Controllers.getCiscoCodeExceptionWindow());
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: requestTemplateWindowException", e);
		}
		if (popup != null) {
			Stage ciscoCodeException = new Stage();
			ciscoCodeException.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
			ciscoCodeException.setResizable(false);
			ciscoCodeException.setAlwaysOnTop(true);
			ciscoCodeException.setTitle("Exception E-mailer");
			ciscoCodeException.setScene(popup);
			ciscoCodeException.setOnCloseRequest(windowEvent -> Controllers.removeCiscoCodeExceptionWindow());
			ciscoCodeException.show();
			ciscoCodeException.requestFocus();
		} // else we already have an exception open and in progress.
	}

	public void requestTemplateWindowFollowUp() {
		Scene popup = null;
		try {
			popup = new Scene(Controllers.getFollowUpWindow());
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: requestTemplateWindowFollowUp", e);
		}
		if (popup != null) {
			Stage followUp = new Stage();
			followUp.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
			followUp.setResizable(false);
			followUp.setTitle("Follow up Template");
			followUp.setScene(popup);
			followUp.setOnCloseRequest(windowEvent -> Controllers.removeFollowUpWindow());
			followUp.show();
			followUp.requestFocus();
		}
	}

	public void requestTemplateWindowMissedEvent() {
		Scene popup = null;
		try {
			popup = new Scene(Controllers.getMissedEventWindow());
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: requestTemplateWindowMissedEvent", e);
		}
		if (popup != null) {
			Stage eventWindow = new Stage();
			eventWindow.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
			eventWindow.setResizable(false);
			eventWindow.setTitle("Missed/Late Event");
			eventWindow.setScene(popup);
			eventWindow.setOnCloseRequest(windowEvent -> Controllers.removeMissedEventWindow());
			eventWindow.show();
			eventWindow.setOnShown(event -> eventWindow.requestFocus());
		}
	}

	public void requestTemplateWindowScheduleCallback() {
		Scene popup = null;
		try {
			popup = new Scene(Controllers.getCallbackWindow());
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: requestTemplateWindowScheduleCallback", e);
		}
		if (popup != null) {
			Stage eventWindow = new Stage();
			eventWindow.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
			eventWindow.setResizable(false);
			eventWindow.setTitle("Scheduled Callback");
			eventWindow.setScene(popup);
			eventWindow.setOnCloseRequest(windowEvent -> Controllers.removeCallbackWindow());
			eventWindow.show();
			eventWindow.setOnShown(event -> eventWindow.requestFocus());
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
		Stage mainWindow = (Stage) Controllers.getMainWindow().getScene().getWindow();
		Stage docuNamer = new Stage();
		docuNamer.setResizable(false);
		docuNamer.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
		docuNamer.setTitle("Document Renaming");
		docuNamer.setScene(new Scene(Controllers.getDocumentRenamingWindow()));
		docuNamer.setOnShown(windowEvent -> mainWindow.hide());
		docuNamer.setOnHidden(windowEvent -> reopenMainWindow());
		docuNamer.setOnCloseRequest(windowEvent -> reopenMainWindow());

		docuNamer.show();
	}

	private void reopenMainWindow() {
		Controllers.removeDocumentRenamingWindow();
		((Stage) Controllers.getMainWindow().getScene().getWindow()).show();
	}

	public void prepTheHounds() {
		Scene docuHoundScene = null;
		try {
			docuHoundScene = new Scene(Controllers.getDocuHoundWindow());
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: prepTheHounds", e);
		}
		if (docuHoundScene != null) {
			Stage docuPound = new Stage();
			docuPound.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
			docuPound.setResizable(false);
			docuPound.setTitle("Document DocumentHound");
			docuPound.setScene(docuHoundScene);
			docuPound.setOnCloseRequest(windowEvent -> Controllers.removeDocuHoundWindow());
			docuPound.show();
			docuPound.requestFocus();
		}
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

	enum CallType {
		INBOUND, OUTBOUND
	}
}
