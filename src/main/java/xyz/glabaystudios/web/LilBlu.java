package xyz.glabaystudios.web;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;

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


		Scene scene = new Scene(Controllers.getMainWindow());
		mainWindow.setResizable(false);
		mainWindow.setAlwaysOnTop(true);
		mainWindow.getIcons().add(new Image(String.valueOf(getClass().getResource("lilblu.png"))));
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

	public static void main(String... args) {
		launch(args);
	}
}
