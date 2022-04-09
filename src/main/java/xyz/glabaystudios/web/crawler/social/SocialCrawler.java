package xyz.glabaystudios.web.crawler.social;

import lombok.Getter;
import org.jsoup.select.Elements;
import xyz.glabaystudios.web.crawler.Crawler;
import xyz.glabaystudios.web.model.social.SocialLink;

import java.util.HashMap;
import java.util.Map;

public class SocialCrawler extends Crawler {

	@Getter Map<String, SocialLink> socialLinkMap;

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
		for (String key : SocialLink.getSocialKeys()) {
			if (href.toLowerCase().contains(key.toLowerCase())) {
				String[] socialPlatformAndSocialDisplayName = href.toLowerCase().split(".com");
				String displayName = socialPlatformAndSocialDisplayName[socialPlatformAndSocialDisplayName.length - 1].replace("/", "");
				SocialLink dto = new SocialLink();
				dto.setSocialPlatformURL(href.toLowerCase());
				dto.setSocialDisplayName(displayName);
				socialLinkMap.put(key, dto);
			}
		}
	}
}
