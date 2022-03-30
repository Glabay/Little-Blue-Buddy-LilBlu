package xyz.glabaystudios.web.gui.templates.emails;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import xyz.glabaystudios.smtp.ExceptionEmail;
import xyz.glabaystudios.smtp.Recipients;
import xyz.glabaystudios.util.TimeZoneWrapper;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.model.exceptions.ScheduledCallback;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CallbackWindow implements Initializable {

	@FXML public TextArea rebuttalReasonOne;
	@FXML public TextArea rebuttalReasonTwo;
	@FXML public TextField rebuttalOneTimeframeField;
	@FXML public TextField rebuttalTimeframeTwoField;

	@FXML public TextField callbackDateField;
	@FXML public Spinner<Integer> callbackTimeHr;
	@FXML public Spinner<Integer> callbackTimeMin;
	@FXML public ToggleButton callbackMeridianBtn;

	@FXML public Button copyFollowUp;
	@FXML public Button discardExpBtn;
	@FXML public Button emailCallbackBtn;

	@FXML public TextField caseField;
	@FXML public TextField callbackLeftOffAtField;
	@FXML public ComboBox<String> timezoneComboBox;
	@FXML public TextArea callbackDetailedReasonArea;
	@FXML public TextField countryCodeField;

	Alert missingFields = new Alert(Alert.AlertType.ERROR);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		callbackDateField.setText(DateTimeFormatter.ofPattern("MM-dd-yyyy").format(LocalDateTime.now()));
		callbackTimeHr.setValueFactory(getSpinner(1, 12));
		callbackTimeMin.setValueFactory(getSpinner(0, 59));
		callbackTimeHr.getEditor().setText("");
		callbackTimeMin.getEditor().setText("");
		timezoneComboBox.setDisable(true);
		callbackDetailedReasonArea.setWrapText(true);
		rebuttalReasonOne.setWrapText(true);
		rebuttalReasonTwo.setWrapText(true);
		missingFields.initOwner(Controllers.getCallbackWindow().getScene().getWindow());
	}

	@FXML
	public ObservableList<String> getTimeZoneList(String countryCode) {
		List<String> timezones = new ArrayList<>();
		timezones.add("Select Customers Timezone");
		timezones.addAll(TimeZoneWrapper.getWrapper().getTimeZonePhoneBook(countryCode));
		return FXCollections.observableArrayList(timezones);
	}

	private SpinnerValueFactory.IntegerSpinnerValueFactory getSpinner(int min, int max) {
		return new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0);
	}

	/**
	 * Can Continue
	 * <br>
	 * Provides a quick and clear checking for a few required fields.
	 * @return true if we have met the requirements
	 */
	private boolean canContinue() {
		missingFields.setTitle("Somethings not quite right here.");
		if (callbackTimeMin.getEditor().getText().isEmpty() || callbackTimeHr.getEditor().getText().isEmpty()) {
			missingFields.setContentText("Please fill out the Callback Time");
			return false;
		}
		if (callbackDateField.getText().isEmpty()) {
			missingFields.setContentText("What date are we looking to make this callback?");
			return false;
		}
		if (caseField.getText().isEmpty()) {
			missingFields.setContentText("What case are we calling back?");
			return false;
		}
		if (timezoneComboBox.getSelectionModel().getSelectedIndex() == 0) {
			missingFields.setContentText("What is the Customers timezone?");
			return false;
		}
		if (callbackLeftOffAtField.getText().isEmpty()) {
			missingFields.setContentText("Where abouts did you leave off with this case?");
			return false;
		}
		if (rebuttalReasonOne.getText().isEmpty()/* || rebuttalOneTimeframeField.getText().isEmpty()*/) {
			missingFields.setContentText("What can you tell us about your first rebuttal?");
			return false;
		}
		if (rebuttalReasonTwo.getText().isEmpty()/* || rebuttalTwoTimeframeField.getText().isEmpty()*/) {
			missingFields.setContentText("What can you tell us about your second rebuttal?");
			return false;
		}
		return true;
	}

	public void sendCloseAction() {
		Controllers.getCallbackWindow().getScene().getWindow().hide();
		Controllers.removeCallbackWindow();
	}

	public void copyResultToClipboard() {
		if (canContinue()) {
			Toolkit.getDefaultToolkit()
					.getSystemClipboard()
					.setContents(new StringSelection(getScheduledCallback().toString()), null);
		} else missingFields.show();
	}

	public void toggleCallbackMeridiem(ActionEvent actionEvent) {
		ToggleButton btn = (ToggleButton) actionEvent.getSource();
		btn.setText(btn.isSelected() ? "PM" : "AM");
	}

	public void sendScheduledCallbackToSpokan() {
		if (canContinue()) {
			emailCallbackBtn.setDisable(true);
			new ExceptionEmail("Scheduled Callback - " + System.getProperty("user.name"))
					.setRecipients(Recipients.SPOKAN)
					.setEmailMessage(getScheduledCallback().toString())
					.sendException();
			emailCallbackBtn.setText("Sent!");
		} else missingFields.show();
	}

	private String getTime() {
		int min = Integer.parseInt(callbackTimeMin.getEditor().getText());
		String minuet = (min < 10) ? "0" + min : String.valueOf(min);
		return callbackTimeHr.getEditor().getText() + ":" + minuet + " " + callbackMeridianBtn.getText();
	}

	private ScheduledCallback getScheduledCallback() {
		ScheduledCallback callback = new ScheduledCallback();

		callback.setAgent(System.getProperty("user.name"));
		callback.setInterview(caseField.getText());
		callback.setDate(callbackDateField.getText());
		callback.setCallbackTime(getTime());
		callback.setTimezone(timezoneComboBox.getSelectionModel().getSelectedItem().split("\\|")[1].trim());
		callback.setCallbackReason(callbackDetailedReasonArea.getText());
		callback.setLeftOffAt(callbackLeftOffAtField.getText());
		callback.setRebuttalOne(rebuttalReasonOne.getText());
		callback.setRebuttalOneTime(rebuttalOneTimeframeField.getText());
		callback.setRebuttalTwo(rebuttalReasonTwo.getText());
		callback.setRebuttalTwoTime(rebuttalTimeframeTwoField.getText());

		return callback;
	}

	public void handleCountryLookup() {
		countryCodeField.setText(countryCodeField.getText().replaceAll("[^\\D]", "").toUpperCase());
		countryCodeField.positionCaret(countryCodeField.getText().length());
		if (countryCodeField.getText().length() >= 3) {
			countryCodeField.setText(countryCodeField.getText().substring(0, 2));
			countryCodeField.positionCaret(countryCodeField.getText().length());
		}
		String countryCode = countryCodeField.getText().trim();
		if (countryCode.length() == 2) {
			System.out.println(countryCode);
			timezoneComboBox.setItems(getTimeZoneList(countryCode.toUpperCase()));
			timezoneComboBox.getSelectionModel().select(0);
			timezoneComboBox.setDisable(false);
		} else if (countryCode.length() < 2 && !timezoneComboBox.isDisable()) {
			timezoneComboBox.setDisable(true);
			timezoneComboBox.getItems().clear();
		}
	}
}
