package xyz.glabaystudios.web.model.exceptions;

import lombok.Data;

import java.util.Locale;

@Data
public class CiscoCodeException {
	private String ciscoCodeStatus;
	private String timeStarted;
	private String timeEnded;
	private String date;
	private String agent;
	private String approvedBy;
	private String reasonForException;

	@Override
	public String toString() {
		return String.format(Locale.getDefault(),
				"Date: %s%nCode: %s%nTime(AST): %s - %s%nApproved by: %s%nAgent: %s%nReason: %s",
				date,
				ciscoCodeStatus,
				timeStarted,
				timeEnded,
				approvedBy,
				agent,
				reasonForException
		);
	}
}
