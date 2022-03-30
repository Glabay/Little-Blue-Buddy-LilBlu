package xyz.glabaystudios.web.gui.templates.notes;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import xyz.glabaystudios.web.Controllers;
import xyz.glabaystudios.web.model.exceptions.FollowUp;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FollowUpWindow implements Initializable {

	public TextArea followUpNotes;
	public Spinner<Integer> exceptionTimeStartHr;
	public Spinner<Integer> exceptionTimeStartMin;
	public Spinner<Integer> followUpDay;
	public Spinner<Integer> followUpAttempt;
	public ToggleButton exceptionTimeStartAmPm;
	public Button copyFollowUp;
	public Button discardExpBtn;
	public TextField exceptionDateField;
	public TextField caseField;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		exceptionDateField.setText(DateTimeFormatter.ofPattern("MM-dd-yyyy").format(LocalDateTime.now()));
		exceptionTimeStartHr.setValueFactory(getSpinner(1, 12, getStartHr()));
		exceptionTimeStartMin.setValueFactory(getSpinner(0, 59, getStartMin()));
		exceptionTimeStartAmPm.setSelected(LocalDateTime.now().getHour() >= 12);
		exceptionTimeStartAmPm.setText(exceptionTimeStartAmPm.isSelected() ? "PM" : "AM");
		followUpDay.setValueFactory(getSpinner(1, 12, 0));
		followUpAttempt.setValueFactory(getSpinner(1, 2, 0));
		followUpNotes.setWrapText(true);
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

	private String getStartTime() {
		int min = Integer.parseInt(exceptionTimeStartMin.getEditor().getText());
		String minuet = (min < 10) ? "0" + min : String.valueOf(min);
		return exceptionTimeStartHr.getEditor().getText() + ":" + minuet + " " + exceptionTimeStartAmPm.getText();
	}

	public void sendCloseAction() {
		Controllers.getFollowUpWindow().getScene().getWindow().hide();
		Controllers.removeFollowUpWindow();
	}

	public void toggleAmPmBtn(ActionEvent actionEvent) {
		ToggleButton btn = (ToggleButton) actionEvent.getSource();
		btn.setText(btn.isSelected() ? "PM" : "AM");
	}

	Alert unknownCase = new Alert(Alert.AlertType.CONFIRMATION,  "What case are you following up on?.", ButtonType.CLOSE, ButtonType.OK);

	public void copyResultToClipboard() {
		if (caseField.getText().isEmpty()) {
			unknownCase.initOwner(Controllers.getFollowUpWindow().getScene().getWindow());
			unknownCase.show();
			return;
		}
		FollowUp followUp = new FollowUp();
		followUp.setInterview(caseField.getText());
		followUp.setAgent(System.getProperty("user.name"));
		followUp.setDate(exceptionDateField.getText());
		followUp.setDay(followUpDay.getEditor().getText());
		followUp.setAttempt(followUpAttempt.getEditor().getText());
		followUp.setFollowUpTime(getStartTime());
		followUp.setFollowUpResult(followUpNotes.getText().isEmpty() ? followUpNotes.getPromptText() : followUpNotes.getText());

		Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.setContents(new StringSelection(followUp.toString()), null);
	}
}
