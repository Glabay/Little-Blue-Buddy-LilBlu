package xyz.glabaystudios.web;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import xyz.glabaystudios.net.NetworkExceptionHandler;

import java.io.IOException;
import java.util.Properties;

public class LilBlu extends Application {

	double mainWinX = -1;
	double mainWinY = -1;

	@Getter
	static final Properties properties = new Properties();

	@Override
	public void start(Stage mainWindow) {
		try {
			properties.load(getClass().getResourceAsStream("lilblu.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Image lilBluIcon = new Image(String.valueOf(getClass().getResource("lilblu.png")));

		Scene scene = new Scene(Controllers.getMainWindow());
		mainWindow.setResizable(false);
		mainWindow.setAlwaysOnTop(true);
		mainWindow.getIcons().add(lilBluIcon);
		mainWindow.setTitle("Little Blue Buddy");
		mainWindow.setScene(scene);
		mainWindow.setOnHidden(windowEvent -> {
			mainWinX = Controllers.getMainWindow().getScene().getWindow().getX();
			mainWinY = Controllers.getMainWindow().getScene().getWindow().getY();
		});
		mainWindow.setOnShown(windowEvent -> {
			if (mainWinX > 0) mainWindow.setX(mainWinX);
			if (mainWinY > 0) mainWindow.setY(mainWinY);
		});
		mainWindow.show();
	}

	public static void openAbout() {
		Scene about = null;
		try {
			about = new Scene(Controllers.getAboutGlabayStudiosWindow());
		} catch (IllegalArgumentException e) {
			NetworkExceptionHandler.handleException("Trying to open a second window: requestAboutWindow", e);
		}
		if (about != null) {
			Stage aboutWindow = new Stage();
			aboutWindow.getIcons().add(new Image(String.valueOf(LilBlu.class.getResource("lilblu.png"))));
			aboutWindow.setResizable(false);
			aboutWindow.setAlwaysOnTop(true);
			aboutWindow.setTitle("About - Little Blue Buddy");
			aboutWindow.setScene(about);
			aboutWindow.setOnCloseRequest(windowEvent -> Controllers.removeAboutGlabayStudiosWindow());
			aboutWindow.show();
			aboutWindow.setOnShown(windowEvent -> aboutWindow.toFront());
		}
	}

	public static void main(String... args) {
		launch(args);
	}
}
