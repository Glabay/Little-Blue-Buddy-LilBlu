package xyz.glabaystudios.web.model.social;

import lombok.Data;
import lombok.Getter;

@Data
public class SocialLink {

	@Getter
	private static final String[] socialKeys = {
			"Facebook",
			"YouTube",
			"Instagram",
			"Twitter",
			"LinkedIn",
			"Pinterest",
			"TikTok",
			"Reddit",
			"spotify",
			"soundcloud",
			"itunes.apple",
			"snapchat",
			"bandcamp.com"
	};

	private String socialPlatformURL;
	private String socialDisplayName;
}
