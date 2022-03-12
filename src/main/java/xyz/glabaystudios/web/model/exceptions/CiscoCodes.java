package xyz.glabaystudios.web.model.exceptions;

import lombok.Getter;

public enum CiscoCodes {

	SPECIAL_PROJECT ("Special Project"),
	COACHING        ("Coaching"),
	MEETING         ("Meeting"),
	TRAINING        ("Training"),
	PRE_SHIFT       ("Pre-Shift"),
	TECH_ISSUE      ("Technical Issue"),
	OTHER           ("Other")
	;

	CiscoCodes(String codeName) {
		this.codeName = codeName;
	}

	@Getter private final String codeName;
}
