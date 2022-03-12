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
import xyz.glabaystudios.web.model.exceptions.CiscoCodeException;
import xyz.glabaystudios.web.model.exceptions.CiscoCodes;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CiscoCodeExceptionWindows implements Initializable {

	@FXML public TextField exceptionDateField;
	@FXML public Spinner<Integer> exceptionTimeStartHr;
	@FXML public Spinner<Integer> exceptionTimeStartMin;
	@FXML public ToggleButton exceptionTimeStartAmPm;
	@FXML public Spinner<Integer> exceptionTimeFinishedHr;
	@FXML public Spinner<Integer> exceptionTimeFinishedMin;
	@FXML public TextArea exceptionExplanation;
	@FXML public ToggleButton exceptionTimeFinishedAmPm;
	@FXML public TextField exceptionApprovedBy;
	@FXML public Button sendExpBtn;
	@FXML public Button discardExpBtn;
	@FXML public ComboBox<String> exceptionType;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		exceptionDateField.setText(DateTimeFormatter.ofPattern("MM-dd-yyyy").format(LocalDateTime.now()));
		exceptionTimeStartHr.setValueFactory(getSpinner(1, 12, getStartHr()));
		exceptionTimeStartMin.setValueFactory(getSpinner(0, 59, getStartMin()));
		exceptionTimeStartAmPm.setSelected(LocalDateTime.now().getHour() >= 12);
		exceptionTimeFinishedAmPm.setSelected(exceptionTimeStartAmPm.isSelected());
		exceptionTimeStartAmPm.setText(exceptionTimeStartAmPm.isSelected() ? "PM" : "AM");
		exceptionType.setItems(getCodeNames());
		exceptionType.getSelectionModel().select(0);
		exceptionExplanation.setWrapText(true);
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
		List<String> codes = Arrays.stream(CiscoCodes.values()).map(CiscoCodes::getCodeName).collect(Collectors.toCollection(() -> new ArrayList<>(CiscoCodes.values().length)));
		return FXCollections.observableArrayList(codes);
	}

	public void sendExceptionEmail() {
		if (exceptionExplanation.getText().isEmpty()) {
			new Alert(Alert.AlertType.WARNING,
					"Please provide a reason for this exception.",
					ButtonType.CLOSE,
					ButtonType.OK)
					.show();
			return;
		}
		if (exceptionTimeFinishedMin.getEditor().getText().isEmpty() || exceptionTimeFinishedHr.getEditor().getText().isEmpty()) {
			exceptionTimeFinishedHr.setValueFactory(getSpinner(1, 12, getStartHr()));
			exceptionTimeFinishedMin.setValueFactory(getSpinner(0, 59, getStartMin()));
			exceptionTimeFinishedAmPm.setSelected(LocalDateTime.now().getHour() >= 12);
		}
		CiscoCodeException ciscoCodeException = new CiscoCodeException();
		ciscoCodeException.setCiscoCodeStatus(exceptionType.getSelectionModel().getSelectedItem());
		ciscoCodeException.setTimeStarted(getStartTime());
		ciscoCodeException.setTimeEnded(getFinishTime());
		ciscoCodeException.setDate(exceptionDateField.getText());
		ciscoCodeException.setAgent(System.getProperty("user.name"));
		ciscoCodeException.setApprovedBy(exceptionApprovedBy.getText().isEmpty() ? "Not Applicable" : exceptionApprovedBy.getText());
		ciscoCodeException.setReasonForException(exceptionExplanation.getText());

		new ExceptionEmail(ciscoCodeException.getCiscoCodeStatus())
				.setRecipients(Recipients.DEVELOPMENT)
				.setEmailMessage(ciscoCodeException.toString())
				.sendException();
		sendCloseAction();
	}

	private String getStartTime() {
		int min = Integer.parseInt(exceptionTimeStartMin.getEditor().getText());
		String minuet = (min < 10) ? "0" + min : String.valueOf(min);
		return exceptionTimeStartHr.getEditor().getText() + ":" + minuet + " " + exceptionTimeStartAmPm.getText();
	}

	private String getFinishTime() {
		return exceptionTimeFinishedHr.getEditor().getText() + ":" + exceptionTimeFinishedMin.getEditor().getText() + " " + exceptionTimeFinishedAmPm.getText();}

	public void sendCloseAction() {
		Controllers.getCiscoCodeExceptionWindow().getScene().getWindow().hide();
		Controllers.removeCiscoCodeExceptionWindow();
	}

	public void toggleAmPmBtn(ActionEvent actionEvent) {
		ToggleButton btn = (ToggleButton) actionEvent.getSource();
		btn.setText(btn.isSelected() ? "PM" : "AM");
	}
}
