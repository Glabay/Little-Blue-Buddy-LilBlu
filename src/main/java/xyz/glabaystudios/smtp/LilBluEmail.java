package xyz.glabaystudios.smtp;

import javax.mail.Message;
import javax.mail.Session;
import java.util.List;

public abstract class LilBluEmail {

	protected static String EMAIL_SUBJECT = "Test Subject";
	protected static String EMAIL_BODY = "Hello there,\nThis is an example of and should be reported to 'Mike Glabay' if this continues to happen.\n\nThank you,\n\nMike Glabay\nGlabayStudios@Outlook.com";

	protected List<Recipients> recipientsList;

	protected Session emailSession;
	protected Message emailMessage;

	protected void setEmailBody(String message) {
		EMAIL_BODY = message;
	}

	public abstract void sendException();
}
