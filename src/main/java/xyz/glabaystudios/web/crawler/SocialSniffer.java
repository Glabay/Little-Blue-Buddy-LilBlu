package xyz.glabaystudios.web.crawler;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.model.SocialLink;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SocialSniffer {

	final Document page;

	final String userAgent = "Mozilla/5.0 (Windows NT 6.1; rv:80.0) Gecko/27132701 Firefox/78.7";

	String domain;

	@Getter Map<String, SocialLink> socialLinkMap;

	public SocialSniffer(String domain, boolean usingSecureConnection) {
		this.domain = domain.toLowerCase();
		page = getContent(usingSecureConnection);
		socialLinkMap = new HashMap<>();
		lookForSocialMediaLinks();
	}

	String[] socialKeys = {"Facebook", "YouTube", "Instagram", "Twitter", "LinkedIn", "Pinterest", "TikTok", "Reddit"};

	private void lookForSocialMediaLinks() {
		if (page == null) return;
		Elements links = page.getElementsByAttribute("href");

		for (Element link : links) {
			String href = link.attr("href");
			if (!href.contains("www.")) continue;
			if (href.contains("www.facebook.com/pages")) continue;
			Arrays.stream(socialKeys)
					.filter(key -> href.toLowerCase().contains(key.toLowerCase()))
					.forEach(key -> {
						String[] socialPlatformAndSocialDisplayName = href.toLowerCase().split("\\.com");
						String displayName = socialPlatformAndSocialDisplayName[1].replace("/", "");
						SocialLink dto = new SocialLink();
						dto.setSocialPlatformURL(href.toLowerCase());
						dto.setSocialDisplayName(displayName);
						socialLinkMap.put(key, dto);
					});
		}
	}

	Document getContent(boolean usingSecureConnection) {
		try { // try it with the Secure first
			return Jsoup.connect((usingSecureConnection ? "https://" : "http://") + domain).userAgent(userAgent).timeout(4200).get();
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("getContent -> InputOutput" + domain, e);
			//TODO: Throw a message to the user that the social Links timed out, maybe implement a social refresh?
			return null;
		}
	}
}
