package xyz.glabaystudios.web.model;

import lombok.Getter;
import org.apache.commons.net.whois.WhoisClient;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.crawler.SocialSniffer;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
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
		result.domainName = DOMAIN.toUpperCase();
		int mxCount = doLookup();
		result.hasMailServer = mxCount > 0;
		if (mxCount > 0) result.mailServerCount = mxCount;

		Arrays.stream(dumpWhoisResultsFromDomain().split("\r\n")).forEach(line -> {
			if (line.toLowerCase().contains("updated date:")) result.updatedDate = (line.split(": "))[1].split("T")[0];
			if (line.toLowerCase().contains("creation date:")) result.createdDate = (line.split(": "))[1].split("T")[0];
			if (line.toLowerCase().contains("registry expiry date:")) result.registryExpiryDate = (line.split(": "))[1].split("T")[0];
			if (line.toLowerCase().contains("registrar:")) result.registrar = line.split(": ")[1];
			if (line.toLowerCase().contains("name server:")) result.nameServers.add(line.split(": ")[1]);
		});
		if (result.registrar == null && result.hasMailServer) {
			// verify the mail server and the domain we're looking at are the same "host"
			String[] searchingDomain = DOMAIN.split("[.]");
			String[] mxServers = result.mailServers.get(0).split("[.]");
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
		} else if (result.registrar == null) {
			error("registrar is null");
		} else if (result.registrar.equalsIgnoreCase("REGISTER.COM, INC.") || result.registrar.equalsIgnoreCase("NETWORK SOLUTIONS, INC."))
			result.isInFamily = true;

		if (canValidateDate()) result.isNewlyRegistered = result.isDomainNewlyCreated();
		else result.isNewlyRegistered = false;


		checkSecureConnection();

		result.socialLinkMap = new SocialSniffer(result.domainName, result.sslSecure).getSocialLinkMap();
	}

	private void error(String errorMessageFlag) {
		String message = "Error: %s, please reference: https://www.whois.com/whois/%s";
		System.out.printf(Locale.getDefault(), (message) + "%n", errorMessageFlag, getResult().getDomainName().toLowerCase());
		//TODO: Make a GET request of a specific who-is site, and then scrape the data
		//      If this returns a 404 error or 5XX then provide a button on the UI to open a browser with the URI and Parameters provided for quick intel
	}

	boolean canValidateDate() {
		return result.createdDate != null && result.registryExpiryDate != null;
	}

	private void checkSecureConnection() {
		try {
			URL url = new URL("https://" + DOMAIN);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setConnectTimeout(750);
			dumpCertData(con);
		} catch (java.net.SocketTimeoutException e) {
			NetworkExceptionHandler.handleException("checkSecureConnection -> java.net.SocketTimeout", e);
			result.sslSecure = false;
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("checkSecureConnection -> InputOutput", e);
			result.sslSecure = false;
		}
	}

	private void dumpCertData(HttpsURLConnection con) {
		if (con != null) {
			try {
				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");
				result.sslSecure = true;

				Certificate[] certs = con.getServerCertificates();
				for (Certificate cert : certs) {
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
				}
			} catch (SSLHandshakeException e) {
				NetworkExceptionHandler.handleException("dumpCertData -> SSLHandshake", e);
				result.sslSecure = false;
			} catch (java.net.SocketTimeoutException e) {
				NetworkExceptionHandler.handleException("dumpCertData -> java.net.SocketTimeout", e);
				result.sslSecure = false;
			} catch (UnknownHostException e) {
				NetworkExceptionHandler.handleException("dumpCertData -> UnknownHost", e);
				result.sslSecure = false;
			}  catch (IOException e) {
				NetworkExceptionHandler.handleException("dumpCertData -> InputOutput", e);
				result.sslSecure = false;
				System.out.println("Failed due to an IO-Exception - 4.");
			} catch (Exception e) {
				NetworkExceptionHandler.handleException("dumpCertData -> Default", e);
				result.sslSecure = false;
				System.out.println("Failed due to Unknown Exception - 5.");
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

	String queryWithWhoisServer(String domainName) {
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

	String getWhoisServer(String whois) {
		String result = "";
		matcher = pattern.matcher(whois);
		while (matcher.find()) result = matcher.group(1);
		return result;
	}

	int doLookup() {
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
				for (int i = 0; i < attr.size(); i++) result.mailServers.add(attr.get(i).toString());
			}
		} catch (NamingException e) {
			NetworkExceptionHandler.handleException("doLookup -> Naming", e);
		}
		if (attr == null) return(0);
		return (attr.size());
	}
}
