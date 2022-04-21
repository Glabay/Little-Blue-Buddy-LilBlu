package xyz.glabaystudios.web.model.whois;

import lombok.Getter;
import org.apache.commons.net.whois.WhoisClient;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.crawler.social.SocialCrawler;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhoisLookup {

	final String DOMAIN;

	@Getter Whois result;

	static Pattern pattern;
	Matcher matcher;

	static final String WHOIS_SERVER_PATTERN = "Whois Server:\\s(.*)";

	static {
		pattern = Pattern.compile(WHOIS_SERVER_PATTERN);
	}

	public WhoisLookup(String domain) {
		this.DOMAIN = domain;
	}

	public void filterDumpedData() {
		result = new Whois();
		getResult().setDomainName(DOMAIN.toUpperCase());
		int mxCount = doLookup();
		getResult().setHasMailServer(mxCount > 0);
		if (mxCount > 0) getResult().setMailServerCount(mxCount);

		Arrays.stream(dumpWhoisResultsFromDomain().split("\r\n")).forEach(line -> {
			if (line.toLowerCase().contains("updated date:")) getResult().setUpdatedDate((line.split(": "))[1].split("T")[0]);
			if (line.toLowerCase().contains("creation date:")) getResult().setCreatedDate((line.split(": "))[1].split("T")[0]);
			if (line.toLowerCase().contains("registry expiry date:")) getResult().setRegistryExpiryDate((line.split(": "))[1].split("T")[0]);
			if (line.toLowerCase().contains("registrar:")) getResult().setRegistrar(line.split(": ")[1]);
			if (line.toLowerCase().contains("name server:")) getResult().getNameServers().add(line.split(": ")[1]);
		});
		if (getResult().getRegistrar() == null && getResult().isHasMailServer()) {
			// verify the mail server and the domain we're looking at are the same "host"
			String[] searchingDomain = DOMAIN.split("[.]");
			String[] mxServers = getResult().getMailServers().get(0).split("[.]");
			String parent = null;
			// loop over the broken 'sub-domains' on the mail server and if it matches up with the searching domain (minus the extension) best guess is it's the parent
			for (int i = 0; i < mxServers.length; i++) {
				if (mxServers[i].equalsIgnoreCase(searchingDomain[0])) {
					parent = mxServers[i] + "." + mxServers[i + 1];
					break;
				}
			}
			if (parent == null) {
				String message = "Error: %s, please reference: https://who.is/dns/%s";
				System.out.printf(Locale.getDefault(), (message) + "%n", "finding MX Data", getResult().getDomainName().toLowerCase());

			} else {
				// should more or less execute this again with the "parent" domain and then populate the results
				System.out.println(parent);
			}
		} else if (getResult().getRegistrar() == null) {
			String errorMessageFlag = "registrar is null";
			String message = "Error: %s, please reference: https://www.whois.com/whois/%s";
			System.out.printf(Locale.getDefault(), (message) + "%n", errorMessageFlag, getResult().getDomainName().toLowerCase());
		} else if (getResult().getRegistrar().equalsIgnoreCase("REGISTER.COM, INC.") || getResult().getRegistrar().equalsIgnoreCase("NETWORK SOLUTIONS, INC."))
			getResult().setInFamily(true);

		if (canValidateDate()) getResult().setNewlyRegistered(getResult().isDomainNewlyCreated());
		else getResult().setNewlyRegistered(false);

		checkSecureConnection();

		getResult().setSocialLinkMap(new SocialCrawler(getResult().getDomainName(), getResult().isSslSecure()).getSocialLinkMap());
	}

	boolean canValidateDate() {
		return getResult().getCreatedDate() != null && getResult().getRegistryExpiryDate() != null;
	}

	private void checkSecureConnection() {
		try {
			URL url = new URL("https://" + DOMAIN);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setConnectTimeout(1000);
			dumpCertData(con);
		} catch (java.net.SocketTimeoutException e) {
			NetworkExceptionHandler.handleException("checkSecureConnection -> java.net.SocketTimeout", e);
			getResult().setSslSecure(false);
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("checkSecureConnection -> InputOutput", e);
			getResult().setSslSecure(false);
		}
	}

	private void dumpCertData(HttpsURLConnection con) {
		if (con != null) {
			try {
				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");
				getResult().setSslSecure(true);

				Arrays.stream(con.getServerCertificates()).forEach(cert -> {
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
				});
			} catch (SSLException e) {
				NetworkExceptionHandler.handleException("dumpCertData -> SSL", e);
				getResult().setSslSecure(false);
			} catch (IOException e) {
				NetworkExceptionHandler.handleException("dumpCertData -> InputOutput", e);
				getResult().setSslSecure(false);
			} catch (Exception e) {
				NetworkExceptionHandler.handleException("dumpCertData -> Default", e);
				getResult().setSslSecure(false);
			}
		}
	}

	public String dumpWhoisResultsFromDomain() {
		StringBuilder result = new StringBuilder();
		WhoisClient whois = new WhoisClient();
		try {
			whois.connect(WhoisClient.DEFAULT_HOST, 43);
			String whoisData1 = whois.query("=" + DOMAIN);

			String[] cleaned = whoisData1.split(">>>");
			result.append(cleaned[0]);
			whois.disconnect();

			String whoisServerUrl = getWhoisServer(cleaned[0]);
			if (!whoisServerUrl.equals("")) {
				String whoisData2 = queryWithWhoisServer(DOMAIN);
				result.append(whoisData2);
			}
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("dumpWhoIsResultFromDomain -> InputOutput", e);
		}
		return result.toString();

	}

	private String queryWithWhoisServer(String domainName) {
		String result = "";
		WhoisClient whois = new WhoisClient();
		try {
			whois.connect("whois.iana.org");
			result = whois.query(domainName);
			whois.disconnect();
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("queryWithWhoIsServer." + "whois.iana.org" + " -> InputOutput", e);
		}
		System.out.println("DeepDive-Penguin-Result: " + result);
		return result;

	}

	private String getWhoisServer(String whois) {
		String result = "";
		matcher = pattern.matcher(whois);
		while (matcher.find()) result = matcher.group(1);
		return result;
	}

	private int doLookup() {
		Hashtable<String, String> env = new Hashtable<>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		DirContext ictx;
		Attributes attrs;
		Attribute attr = null;
		try {
			ictx = new InitialDirContext(env);
			attrs = ictx.getAttributes( DOMAIN, new String[] { "MX" });
			attr = attrs.get("MX");
			if (attr != null) {
				for (int i = 0; i < attr.size(); i++) getResult().getMailServers().add(attr.get(i).toString());
			}
		} catch (NamingException e) {
			NetworkExceptionHandler.handleException("doLookup -> Naming", e);
		}
		if (attr == null) return(0);
		return (attr.size());
	}
}
