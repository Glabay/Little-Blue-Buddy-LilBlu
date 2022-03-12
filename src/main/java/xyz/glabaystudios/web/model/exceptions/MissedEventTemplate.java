package xyz.glabaystudios.web.model.exceptions;

import lombok.Data;

import java.util.Locale;
import java.util.TimeZone;

@Data
public class MissedEventTemplate {

	private String agent;
	private String date;
	private String event;
	private String timeTaken;
	private String reason;

	@Override
	public String toString() {
		return String.format(Locale.getDefault(),
				"Agent: %s%nDate: %s%nEvent reporting: %s%nTime(%s): %s%nReason: %s",
				agent,
				date,
				event,
				TimeZone.getDefault().getDisplayName(Locale.getDefault()),
				timeTaken,
				reason
		);
	}
}
