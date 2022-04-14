package xyz.glabaystudios.web.gui.pound;

import xyz.glabaystudios.net.NetworkExceptionHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Downloader extends Thread {

	private String downloadDomain;

	private Map<String, String> foundDocuments;

	public void setDomain(String downloadDomain) {
		this.downloadDomain = downloadDomain;
	}

	public void passDocumentList(HashMap<String, String> documentList) {
		this.foundDocuments = documentList;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void run() {
		String domain = downloadDomain + "/";
		while(!isInterrupted()) {
			Arrays.stream(foundDocuments.keySet().toArray(new String[0])).forEach(link -> {
				String home = System.getProperty("user.home");
				String[] split = link.replace(" ", "%20").replace("http://", "").split("/");
				String cleanFileName = split[split.length-1].replace("%20", " ");
				String localPath = home + "/Downloads/" + downloadDomain.replace("http://", "").replace(".", "-");
				File file = new File(localPath + "/" + cleanFileName);
				try {
					if (!file.exists()) {
						File file2 = new File(localPath);
						if (!file2.exists()) new File(localPath).mkdirs();
						file.createNewFile();
					}
					String uri = domain + link.replace(" ", "%20");
					HttpURLConnection connection = (HttpURLConnection) new URL(uri).openConnection();
					connection.setRequestMethod("GET");
					InputStream in = connection.getInputStream();
					FileOutputStream out = new FileOutputStream(file.getCanonicalPath());
					byte[] buf = new byte[2048];
					int n = in.read(buf);
					while (n >= 0) {
						out.write(buf, 0, n);
						n = in.read(buf);
					}
					out.flush();
					out.close();
				} catch (FileNotFoundException e) {
					NetworkExceptionHandler.handleException("run().FileNotFound -> " + file.getName(), e);
				} catch (MalformedURLException e) {
					NetworkExceptionHandler.handleException("run().MalformedURL -> " + file.getName(), e);
				} catch (ProtocolException e) {
					NetworkExceptionHandler.handleException("run().Protocol -> " + file.getName(), e);
				} catch (IOException e) {
					NetworkExceptionHandler.handleException("run().IOException -> " + file.getName(), e);
				}
			});
			interrupt();
		}
	}
}
