package xyz.glabaystudios.smtp;

import lombok.Getter;

public enum Recipients {

	DEVELOPMENT             ("Java_Guru@live.com"),
	GLABAY_STUDIOS          ("GlabayStudios@outlook.com"),
	E_TIME                  ("eTimeChanges@web.com"),
	SUPS_DISTRO             ("Pro-OnboardingSups@web.com"),
	SPOKAN                  ("SpokaneIBWBC@web.com"),
	ONBOARDING_COORDINATORS ("Pro-OnboardingCoordinators@web.com")
	;

	@Getter
	final String emailAddress;

	Recipients(String emailAddress) {
		this.emailAddress = emailAddress;
	}
}
