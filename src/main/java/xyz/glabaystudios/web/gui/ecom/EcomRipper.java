package xyz.glabaystudios.web.gui.ecom;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;
import xyz.glabaystudios.web.LilBlu;
import xyz.glabaystudios.web.crawler.ecomm.EcommCrawler;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class EcomRipper implements Initializable {

	@FXML public ImageView imageView;
	@FXML public ImageView imageView2;
	@FXML public Button productImageDownloadBtn;
	@FXML public TextField productUrlField;
	@FXML public TextField productNameField;
	@FXML public TextField basePriceField;
	@FXML public TextField productWidthField;
	@FXML public TextField productLengthField;
	@FXML public TextField productHeightField;
	@FXML public TextField productWeightField;
	@FXML public TextArea productDescriptionArea;
	@FXML public VBox productOptionsBox;
	@FXML public VBox productOptionsAdjustmentsBox;

	@Override public void initialize(URL url, ResourceBundle resourceBundle) {}

	public void checkForSearchTrigger(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.ENTER && productUrlField.getText().length() > 7) fetchStoreInformation();
	}

	private void fetchStoreInformation() {
		String link = productUrlField.getText();
		EcommCrawler ecommCrawler = EcommCrawler.getCrawlingMerchant(link);
		ecommCrawler.crawlThePageForContent();

		getProductNameField().setAlignment(Pos.CENTER_LEFT);
		getProductNameField().setText(ecommCrawler.getPegaProduct().getProductName().trim());
		getBasePriceField().setText(String.valueOf(ecommCrawler.getPegaProduct().getProductPriceBase()));
		getProductDescriptionArea().setText(ecommCrawler.getPegaProduct().getProductDescription());

		loadImages(ecommCrawler.getPegaProduct().getProductImages());

		HashMap<String, List<String>> options = ecommCrawler.getPegaProduct().getProductOptions();
		HashMap<String, List<String>> optionAdjustments = ecommCrawler.getPegaProduct().getProductOptionPriceAdjustments();
		productOptionsBox.getChildren().clear();
		productOptionsAdjustmentsBox.getChildren().clear();
		populateOptions(options, productOptionsBox);
		populateOptions(optionAdjustments, productOptionsAdjustmentsBox);
		productUrlField.clear();
	}

	private void populateOptions(HashMap<String, List<String>> optionMap, VBox vBox) {
		optionMap.keySet().forEach(name -> {
			Label optionName = new Label(name);
			StringBuilder optionNamesBuilder = new StringBuilder();
			optionMap.get(name).forEach(opt -> optionNamesBuilder.append(opt).append("\n"));
			TextArea optionNamesArea = new TextArea(optionNamesBuilder.toString());
			optionNamesArea.setOnMouseClicked(this::copyText);
			int calculatedHeight = ((optionMap.get(name).size() + 1) * 18);
			optionNamesArea.setPrefHeight(calculatedHeight);
			vBox.getChildren().addAll(optionName, optionNamesArea);
		});
	}

	private void loadImages(ArrayList<String> images) {
		imageView.setImage(new Image(formatProductImageLink(images.get(0))));
		if (images.size() > 1) imageView2.setImage(new Image(formatProductImageLink(images.get(images.size() - 1))));
		else imageView2.setImage(new Image(String.valueOf(LilBlu.class.getResource("newfold-logo-icon.png"))));
	}

	private String formatProductImageLink(String input) {
		if (!input.startsWith("http")) input = "https://" + input;
		return input.replaceAll("(?<!(http:|https:))/+", "/");
	}

	public void copyText(MouseEvent mouseEvent) {
		String toCopy = ((TextInputControl) mouseEvent.getSource()).getText();
		if (toCopy.isEmpty() || toCopy.isBlank()) return;
		Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.setContents(new StringSelection(toCopy), null);
	}

}

