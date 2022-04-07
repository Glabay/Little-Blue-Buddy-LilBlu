package xyz.glabaystudios.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkExceptionHandler {

	public static void handleException(String errorDesc, Exception thrownException) {
		System.out.println(errorDesc);
		if (thrownException == null) {
			System.out.println("Error message was null.");
			return;
		}
		thrownException.printStackTrace();
	}

	private static final HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	/**
	 * Sending an exception to the GlabayStudios Network for Exception logging
	 * Exceptions sent are logged for future improvements and bug fixes,
	 * so the end-user doesn't have to submit the error report as they are sent instead of printed to a console
	 *
	 * @param json The JSON body to post to the ExceptionServer
	 */
	private static void prepareHttpRequest(String json) {
		System.out.println("HTTP POST\n" + json);
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.uri(URI.create("http://localhost:8080/lilblu/api/error")) // TODO
				.setHeader("User-Agent", "Little Blue Buddy - Exception") // add request header
				.header("Content-Type", "application/json")
				.build();
		try {
			httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
