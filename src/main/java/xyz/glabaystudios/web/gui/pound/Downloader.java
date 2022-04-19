package xyz.glabaystudios.web.gui.pound;

import xyz.glabaystudios.net.NetworkExceptionHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
		String localPath = System.getProperty("user.home") + "/Downloads/" + getFormattedNameForFolder();
		while(!isInterrupted()) {
			Arrays.stream(foundDocuments.keySet().toArray(new String[0])).forEach(link -> {
				String[] split = link
						.replace(" ", "%20")
						.replace("http://", "")
						.split("/");
				String cleanFileName = split[split.length-1].replace("%20", " ");
				if (cleanFileName.startsWith("http") && cleanFileName.contains("youtube.com/")) handleLinks(cleanFileName);
				else {
					File file = new File(localPath + "/" + cleanFileName);
					try {
						if (!file.exists()) {
							File file2 = new File(localPath);
							if (!file2.exists()) new File(localPath).mkdirs();
							file.createNewFile();
						}
						String uri = domain + link.replace(" ", "%20").replace("../", "");
						if (link.startsWith("http")) uri = link.replace(" ", "%20");
//					    System.out.println("URI: " + uri);
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
//					NetworkExceptionHandler.handleException("run().FileNotFound -> " + file.getName(), e);
					} catch (MalformedURLException e) {
						NetworkExceptionHandler.handleException("run().MalformedURL -> " + file.getName(), e);
					} catch (ProtocolException e) {
						NetworkExceptionHandler.handleException("run().Protocol -> " + file.getName(), e);
					} catch (IOException e) {
//					NetworkExceptionHandler.handleException("run().IOException -> " + file.getName(), e);
						handleLinks(link);
					}
				}
			});
			interrupt();
		}
	}

	private void handleLinks(String link) {
		String localPath = System.getProperty("user.home") + "/Downloads/" + getFormattedNameForFolder();
		try {
			if (link.contains("<iframe ")) append("*** IFRAME ***\n\n" + link + "\n\n");
			else if (link.contains("watch?v=")) append("*** VIDEO_LINK ***\n\n" + link + "\n\n");
			else if (link.contains("youtube.com/user") || link.contains("youtube.com/c/")) append("*** CHANNEL_LINK ***\n\n" + link + "\n\n");
			else if (link.contains("youtube.com")) append("*** YOUTUBE_LINK ***\n\n" + link + "\n\n");
			else if (link.endsWith(".pdf")) {
				String[] split = link.replace(" ", "%20").replace("http://", "").split("/");
				String cleanFileName = split[split.length-1].replace("%20", " ");
				File file = new File(localPath + "/" + cleanFileName);
				try {
					HttpURLConnection connection = (HttpURLConnection) new URL(link).openConnection();
					int code = connection.getResponseCode();
					if (code == HttpURLConnection.HTTP_FORBIDDEN) {

						return;
					}
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
					NetworkExceptionHandler.handleException("handleLinks -> FileNotFound " + file.getName(), e);
				} catch (MalformedURLException e) {
					NetworkExceptionHandler.handleException("handleLinks -> MalformedURL " + file.getName(), e);
				} catch (ProtocolException e) {
					NetworkExceptionHandler.handleException("handleLinks -> Protocol " + file.getName(), e);
				} catch (IOException e) {
					NetworkExceptionHandler.handleException("handleLinks -> InputOutput " + file.getName(), e);
				}
			}
			else System.out.println("Unhandled: " + link);
		} catch (IOException e) {
			NetworkExceptionHandler.handleException("handleLinks -> " + link, e);
		}
	}

	/**
	 * Append the link we're passing to a txt file within the user's Downloads file with the rest of found files
	 * @param string The Data we're appending to the txt file
	 */
	private void append(String string) throws IOException {
		if (string.isEmpty()) return;
		String localPath = System.getProperty("user.home") + "/Downloads/" + getFormattedNameForFolder();
		String strPath = localPath + "/LilBlu_Links.txt";
		try {
			byte[] data = string.getBytes();
			Files.write(Paths.get(strPath), data, StandardOpenOption.APPEND);
		} catch (NoSuchFileException e) {
			File file = new File(strPath);
			if (!file.exists()) {
				File file2 = new File(localPath);
				if (!file2.exists()) new File(localPath).mkdirs();
				file.createNewFile();
			}
			append(string);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getFormattedNameForFolder() {
		return downloadDomain.replace("http://", "").replace(".", "-");
	}
}
