package xyz.glabaystudios.web.model.exceptions;

import lombok.Data;

import java.util.Locale;

@Data
public class ScheduledCallback {
	private String interview;
	private String agent;
	private String date;
	private String timezone;
	private String callbackTime;
	private String leftOffAt;
	private String callbackReason;
	private String rebuttalOne;
	private String rebuttalOneTime;
	private String rebuttalTwo;
	private String rebuttalTwoTime;

	@Override
	public String toString() {
		return String.format(Locale.getDefault(),
				"%s%nAgent: %s%nDate of Callback: %s%nTime of Callback(%s): %s%nLeft off at: %s%nDetailed Reason for callback: %s%n%nRebuttal 1: %s%nApprox time in the call: %s%n%nRebuttal 2: %s%nApprox time in the call: %s",
				getInterview(),
				getAgent(),
				getDate(),
				getTimezone(),
				getCallbackTime(),
				getLeftOffAt(),
				getCallbackReason(),
				getRebuttalOne(),
				getRebuttalOneTime(),
				getRebuttalTwo(),
				getRebuttalTwoTime()
		);
	}
}
