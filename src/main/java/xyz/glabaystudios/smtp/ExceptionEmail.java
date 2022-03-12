package xyz.glabaystudios.smtp;

import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.LilBlu;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class ExceptionEmail extends LilBluEmail {

	public ExceptionEmail(String emailSubject) {
		Properties prop = System.getProperties();
		prop.put("mail.smtp.host", LilBlu.getProperties().getProperty("smtp.host"));
		prop.put("mail.smtp.port", LilBlu.getProperties().getProperty("smtp.port"));
		prop.put("mail.smtp.auth", LilBlu.getProperties().getProperty("smtp.auth"));
		prop.put("mail.smtp.starttls.enable", LilBlu.getProperties().getProperty("smtp.starttls.enable"));
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(LilBlu.getProperties().getProperty("email.address"), LilBlu.getProperties().getProperty("email.password"));
			}
		};
		EMAIL_SUBJECT = emailSubject;
		emailSession = Session.getInstance(prop, auth);
		emailMessage = new MimeMessage(emailSession);
	}
	
	public ExceptionEmail setRecipients(Recipients primary, Recipients... additional) {
		recipientsList = new ArrayList<>();
		recipientsList.add(primary);
		if (additional.length > 0) Collections.addAll(recipientsList, additional);
		return this;
	}

	private List<InternetAddress[]> getRecipientsEmails() throws AddressException {
		List<InternetAddress[]> mailingList = new ArrayList<>(recipientsList.size());
		for (Recipients recipient : recipientsList) mailingList.add(InternetAddress.parse(recipient.getEmailAddress(), false));

		return mailingList;
	}

	public ExceptionEmail setEmailMessage(String message) {
		setEmailBody(message);
		try {
			emailMessage.addHeader("Content-type", "text/HTML; charset=UTF-8");
			emailMessage.addHeader("format", "flowed");
			emailMessage.addHeader("Content-Transfer-Encoding", "8bit");
			emailMessage.setFrom(new InternetAddress(LilBlu.getProperties().getProperty("email.address"), "Little-Blue-Buddy"));
			for (InternetAddress[] emailRecipient : getRecipientsEmails())
				emailMessage.setRecipients(Message.RecipientType.TO, emailRecipient);
			emailMessage.setSubject(EMAIL_SUBJECT);
			emailMessage.setText(EMAIL_BODY);
			emailMessage.setSentDate(new Date());
		} catch (MessagingException e) {
			NetworkExceptionHandler.handleException("setEmailMessage -> Messaging", e);
		} catch (UnsupportedEncodingException e) {
			NetworkExceptionHandler.handleException("setEmailMessage -> UnsupportedEncoding", e);
		}
		return this;
	}

	@Override
	public void sendException() {
		try {
			Transport.send(emailMessage);
		} catch (SendFailedException e) {
			NetworkExceptionHandler.handleException("sendException -> SendFailed", e);
		} catch (MessagingException e) {
			NetworkExceptionHandler.handleException("sendException -> Messaging", e);
		}
	}
}
