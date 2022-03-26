package xyz.glabaystudios.web.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.LilBlu;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutPanel implements Initializable {

	@FXML public ImageView linkedInIcon;
	@FXML public ImageView facebookIcon;
	@FXML public ImageView discordIcon;
	@FXML public ImageView gitHubIcon;
	@FXML public Label buildDate;
	@FXML public Label versionLabel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		buildDate.setText(String.format(buildDate.getText(), LilBlu.getProperties().getProperty("lilblu.build.date")));
		versionLabel.setText(String.format(versionLabel.getText(), LilBlu.getProperties().getProperty("lilblu.build.version")));
	}

	public void openLink(String link) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(URI.create(link));
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("openLink -> About-link: " + link, e);
			}
		}
	}

	public void openLinkedIn() {
		openLink("https://www.linkedin.com/in/mike-glabay/");
	}

	public void openFacebook() {
		openLink("https://www.facebook.com/GlabayStudios");
	}

	public void openGitHub() {
		openLink("https://github.com/Glabay");
	}

	public void openDiscord() {
		openLink("https://discord.gg/9F8Pc7wWnT");
	}

	public void openNewfoldLink() {
		openLink("https://newfold.com/newsroom");
	}
}
