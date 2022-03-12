package xyz.glabaystudios.web.gui.templates.exceptions;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import xyz.glabaystudios.smtp.ExceptionEmail;
import xyz.glabaystudios.smtp.Recipients;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.model.exceptions.Events;
import xyz.glabaystudios.web.model.exceptions.MissedEventTemplate;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MissedEventWindow implements Initializable {

	@FXML public TextField eventDateField;
	@FXML public Spinner<Integer> eventTakenHr;
	@FXML public Spinner<Integer> eventTakenMin;
	@FXML public ToggleButton eventMerridieBtn;
	@FXML public TextArea notesArea;
	@FXML public Button sendEmailBtn;
	@FXML public Button discardEmailBtn;
	@FXML public ComboBox<String> eventType;
	@FXML public Label eventTimeLabel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		eventDateField.setText(DateTimeFormatter.ofPattern("MM-dd-yyyy").format(LocalDateTime.now()));
		eventTakenHr.setValueFactory(getSpinner(1, 12, getStartHr()));
		eventTakenMin.setValueFactory(getSpinner(0, 59, getStartMin()));
		eventMerridieBtn.setSelected(LocalDateTime.now().getHour() >= 12);
		eventMerridieBtn.setText(eventMerridieBtn.isSelected() ? "PM" : "AM");
		eventType.setItems(getCodeNames());
		eventType.getSelectionModel().select(0);
		notesArea.setWrapText(true);
	}

	private SpinnerValueFactory.IntegerSpinnerValueFactory getSpinner(int min, int max, int startingPoint) {
		return new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, startingPoint);
	}

	private int getStartHr() {
		int hour = LocalDateTime.now().getHour();
		if (hour > 12) return hour-12;
		return hour;
	}

	private int getStartMin() {
		return LocalDateTime.now().getMinute();
	}

	@FXML
	public ObservableList<String> getCodeNames() {
		return FXCollections.observableArrayList(Arrays.stream(Events.values()).map(Events::getEventName).collect(Collectors.toCollection(() -> new ArrayList<>(Events.values().length))));
	}

	public void sendEmail() {
		if (notesArea.getText().isEmpty()) {
			new Alert(Alert.AlertType.WARNING,
					"Please provide a reason for this exception.",
					ButtonType.CLOSE,
					ButtonType.OK)
					.show();
			return;
		}
		if (eventTakenHr.getEditor().getText().isEmpty() || eventTakenMin.getEditor().getText().isEmpty()) {
			eventTakenHr.setValueFactory(getSpinner(1, 12, getStartHr()));
			eventTakenMin.setValueFactory(getSpinner(0, 59, getStartMin()));
		}
		MissedEventTemplate emailTemplate = new MissedEventTemplate();
		emailTemplate.setAgent(System.getProperty("user.name"));
		emailTemplate.setDate(eventDateField.getText());
		emailTemplate.setEvent(eventType.getSelectionModel().getSelectedItem());
		emailTemplate.setTimeTaken(getTime());
		emailTemplate.setReason(notesArea.getText());

		new ExceptionEmail(emailTemplate.getEvent())
				.setRecipients(Recipients.DEVELOPMENT)
				.setEmailMessage(emailTemplate.toString())
				.sendException();
		sendCloseAction();
	}

	private String getTime() {
		int min = Integer.parseInt(eventTakenMin.getEditor().getText());
		String minuet = (min < 10) ? "0" + min : String.valueOf(min);
		return eventTakenHr.getEditor().getText() + ":" + minuet + " " + eventMerridieBtn.getText();
	}

	public void sendCloseAction() {
		Controllers.getMissedEventWindow().getScene().getWindow().hide();
		Controllers.removeMissedEventWindow();
	}

	public void toggleMerridie(ActionEvent actionEvent) {
		ToggleButton btn = (ToggleButton) actionEvent.getSource();
		btn.setText(btn.isSelected() ? "PM" : "AM");
	}
}
