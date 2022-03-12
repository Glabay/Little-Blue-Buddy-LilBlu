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
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.model.exceptions.ScheduledCallback;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	Alert missingFields = new Alert(Alert.AlertType.ERROR);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		callbackDateField.setText(DateTimeFormatter.ofPattern("MM-dd-yyyy").format(LocalDateTime.now()));
		callbackTimeHr.setValueFactory(getSpinner(1, 12));
		callbackTimeMin.setValueFactory(getSpinner(0, 59));
		timezoneComboBox.setItems(getTimeZoneList());
		timezoneComboBox.getSelectionModel().select(0);
		callbackDetailedReasonArea.setWrapText(true);
		rebuttalReasonOne.setWrapText(true);
		rebuttalReasonTwo.setWrapText(true);
	}

	@FXML
	public ObservableList<String> getTimeZoneList() {
		List<String> timezones = new ArrayList<>();
		timezones.add("Select Customers Timezone");
		List<String> zones = Stream.of(TimeZone.getAvailableIDs()).filter(zone -> zone != null && zone.length() == 3).collect(Collectors.toList());
		List<String> formattedZones = zones.stream().map(zone -> zone + " | " + TimeZone.getTimeZone(zone).getDisplayName(Locale.getDefault())).collect(Collectors.toCollection(() -> new ArrayList<>(zones.size())));
		timezones.addAll(formattedZones);

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
					.setRecipients(Recipients.DEVELOPMENT, Recipients.GLABAY_STUDIOS)
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
		callback.setTimezone(timezoneComboBox.getSelectionModel().getSelectedItem());
		callback.setCallbackReason(callbackDetailedReasonArea.getText());
		callback.setLeftOffAt(callbackLeftOffAtField.getText());
		callback.setRebuttalOne(rebuttalReasonOne.getText());
		callback.setRebuttalOneTime(rebuttalOneTimeframeField.getText());
		callback.setRebuttalTwo(rebuttalReasonTwo.getText());
		callback.setRebuttalTwoTime(rebuttalTimeframeTwoField.getText());

		return callback;
	}
}
