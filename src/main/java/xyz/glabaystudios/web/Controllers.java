package xyz.glabaystudios.web;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.gui.AboutPanel;
import xyz.glabaystudios.web.gui.DocumentRenamer;
import xyz.glabaystudios.web.gui.pound.DocuHoundWindow;
import xyz.glabaystudios.web.gui.templates.emails.CallbackWindow;
import xyz.glabaystudios.web.gui.templates.exceptions.CiscoCodeExceptionWindows;
import xyz.glabaystudios.web.gui.templates.exceptions.MissedEventWindow;
import xyz.glabaystudios.web.gui.templates.notes.FollowUpWindow;

import java.io.IOException;
import java.util.Objects;

public class Controllers {

	private static Parent mainWindow;
	private static Parent documentRenamingWindow;
	private static Parent ciscoCodeExceptionWindow;
	private static Parent docuHoundWindow;
	private static Parent followUpWindow;
	private static Parent missedEventWindow;
	private static Parent callbackWindow;
	private static Parent aboutGlabayStudiosWindow;

	public static Parent getAboutGlabayStudiosWindow() {
		if (aboutGlabayStudiosWindow == null) {
			try {
				aboutGlabayStudiosWindow = FXMLLoader.load(Objects.requireNonNull(AboutPanel.class.getResource("AboutPanel.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getAboutGlabayStudiosWindow", e);
			}
		}
		return aboutGlabayStudiosWindow;
	}

	public static Parent getCallbackWindow() {
		if (callbackWindow == null) {
			try {
				callbackWindow = FXMLLoader.load(Objects.requireNonNull(CallbackWindow.class.getResource("CallbackTemplate.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getCallbackWindow", e);
			}
		}
		return callbackWindow;
	}

	public static Parent getMissedEventWindow() {
		if (missedEventWindow == null) {
			try {
				missedEventWindow = FXMLLoader.load(Objects.requireNonNull(MissedEventWindow.class.getResource("MissedEventTemplate.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getMissedEventWindow", e);
			}
		}
		return missedEventWindow;
	}

	public static Parent getFollowUpWindow() {
		if (followUpWindow == null) {
			try {
				followUpWindow = FXMLLoader.load(Objects.requireNonNull(FollowUpWindow.class.getResource("FollowUpTemplate.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getFollowUpWindow", e);
			}
		}
		return followUpWindow;
	}

	public static Parent getMainWindow() {
		if (mainWindow == null) {
			try {
				mainWindow = FXMLLoader.load(Objects.requireNonNull(LilBlu.class.getResource("fxPanel.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getMainWindow", e);
			}
		}
		return mainWindow;
	}

	public static Parent getDocuHoundWindow() {
		if (docuHoundWindow == null) {
			try {
				docuHoundWindow = FXMLLoader.load(Objects.requireNonNull(DocuHoundWindow.class.getResource("DocuHound.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getDocuHoundWindow", e);
			}
		}
		return docuHoundWindow;
	}

	public static Parent getCiscoCodeExceptionWindow() {
		if (ciscoCodeExceptionWindow == null) {
			try {
				ciscoCodeExceptionWindow = FXMLLoader.load(Objects.requireNonNull(CiscoCodeExceptionWindows.class.getResource("CiscoCodeException.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getCiscoCodeExceptionWindow", e);
			}
		}
		return ciscoCodeExceptionWindow;
	}

	public static Parent getDocumentRenamingWindow() {
		if (documentRenamingWindow == null) {
			try {
				documentRenamingWindow = FXMLLoader.load(Objects.requireNonNull(DocumentRenamer.class.getResource("DocumentRenamer.fxml")));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("getDocumentRenamingWindow", e);
			}
		}
		return documentRenamingWindow;
	}



	public static void removeCiscoCodeExceptionWindow() {
		ciscoCodeExceptionWindow = null;
	}

	public static void removeDocumentRenamingWindow() {
		documentRenamingWindow = null;
	}

	public static void removeDocuHoundWindow() {
		docuHoundWindow = null;
	}

	public static void removeFollowUpWindow() {
		followUpWindow = null;
	}

	public static void removeMissedEventWindow() {
		missedEventWindow = null;
	}

	public static void removeCallbackWindow() {
		callbackWindow = null;
	}

	public static void removeAboutGlabayStudiosWindow() {
		aboutGlabayStudiosWindow = null;
	}
}
