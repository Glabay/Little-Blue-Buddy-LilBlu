package xyz.glabaystudios.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

		ObjectMapper mapper = new ObjectMapper();
		try {
			String errorJson = mapper.writeValueAsString(thrownException.getCause());
//			prepareHttpRequest(errorJson);
		} catch (JsonProcessingException ignored) {}
		System.out.println(errorDesc);
	}

	private static final HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(10))
			.build();

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
