package xyz.glabaystudios.web.gui;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import xyz.glabaystudios.net.NetworkExceptionHandler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class AboutPanel {

	@FXML public ImageView linkedInIcon;
	@FXML public ImageView facebookIcon;
	@FXML public ImageView discordIcon;
	@FXML public ImageView gitHubIcon;

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
}
