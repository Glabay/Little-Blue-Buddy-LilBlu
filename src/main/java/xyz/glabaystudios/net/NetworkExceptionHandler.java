package xyz.glabaystudios.net;

public class NetworkExceptionHandler {

	public static void handleException(String errorDesc, Exception thrownException) {
		System.out.println(errorDesc);
		if (thrownException != null) thrownException.printStackTrace();
	}
}
