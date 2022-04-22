package xyz.glabaystudios.web.crawler.social;

import lombok.Getter;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.Crawler;
import xyz.glabaystudios.web.model.social.SocialLink;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SocialCrawler extends Crawler {

	@Getter
	private Map<String, SocialLink> socialLinkMap;

	public SocialCrawler(String domain, boolean usingSecureConnection) {
		super(domain, usingSecureConnection);
		crawlThePageForContent();
	}

	public void crawlThePageForContent() {
		if (page == null) return;
		socialLinkMap = new HashMap<>();
		Elements links = page.getElementsByAttribute("href");

		links.stream()
				.map(link -> link.attr("href"))
				.filter(href -> href.contains("www.") || href.contains("http"))
				.filter(href -> href.contains(".com/"))
				.forEach(this::checkHref);
	}

	private void checkHref(String href) {
		Arrays.stream(SocialLink.getSocialKeys())
				.filter(key -> href.toLowerCase().contains(key.toLowerCase()))
				.forEach(key -> {
					String displayName = getDisplayName(href.toLowerCase(), key);
					SocialLink dto = new SocialLink();
					dto.setSocialPlatformURL(href.toLowerCase());
					dto.setSocialDisplayName(displayName);
					socialLinkMap.put(key, dto);
				});
	}

	private String getDisplayName(String string, String key) {
		String displayName;
		if (string.contains("." + key)) {
			displayName = string.replace("https://", "http://")
					.replace("http://", "")
					.replace(key, "")
					.replace(".com", "")
					.replace("open.", "")
					.replace("/artist/", "")
					.replace(".", "")
					.replace("/", "");
		} else {
			String[] socialPlatformAndSocialDisplayName = string.split(".com");
			displayName = socialPlatformAndSocialDisplayName[socialPlatformAndSocialDisplayName.length - 1].replace("/", "");
		}
		return displayName;
	}
}
