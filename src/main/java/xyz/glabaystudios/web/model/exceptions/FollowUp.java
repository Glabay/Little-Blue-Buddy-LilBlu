package xyz.glabaystudios.web.model.exceptions;

import lombok.Data;

import java.util.Locale;
import java.util.TimeZone;

@Data
public class FollowUp {
	private String interview;
	private String agent;
	private String date;
	private String day;
	private String attempt;
	private String followUpTime;
	private String followUpResult;

	@Override
	public String toString() {
		return String.format(Locale.getDefault(),
				"%s%nAgent: %s%nDate: %s%nTime(%s): %s%nDay: %s%nAttempt: %s%nNotes: %s",
				getInterview(),
				getAgent(),
				getDate(),
				getTimeZone(),
				getFollowUpTime(),
				getDay(),
				getAttempt(),
				getFollowUpResult()
		);
	}

	private String getTimeZone() {
		return TimeZone.getDefault().getDisplayName(Locale.getDefault());
	}
}
