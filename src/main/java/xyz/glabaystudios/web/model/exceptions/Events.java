package xyz.glabaystudios.web.model.exceptions;

import lombok.Getter;

public enum Events {

	FIRST_BREAK     ("First break"),
	LUNCH           ("Lunch"),
	SECOND_BREAK    ("Second Break"),
	WORKDAY   ("Workday Event"),
	OTHER           ("Other (Details below)")
	;

	Events(String eventName) {
		this.eventName = eventName;
	}

	@Getter private final String eventName;
}
