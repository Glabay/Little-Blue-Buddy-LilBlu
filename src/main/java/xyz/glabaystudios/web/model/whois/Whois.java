package xyz.glabaystudios.web.model.whois;

import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.model.social.SocialLink;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@NoArgsConstructor
public class Whois {

	private String domainName;
	private String updatedDate;
	private String createdDate;
	private String registryExpiryDate;
	private String registrar;

	private List<String> mailServers = new ArrayList<>();
	private List<String> nameServers = new ArrayList<>();
	private List<String> socialMediaLinks = new ArrayList<>();
	private Map<String, SocialLink> socialLinkMap = new HashMap<>();

	private boolean hasMailServer;
	private boolean newlyRegistered;
	private boolean isInFamily;
	private boolean sslSecure;

	private int mailServerCount;

	private final Byte DAYS_TO_CONSIDER_NEW = 7;

	public boolean isDomainNewlyCreated() {
		try {
			Date compare = getDateToConsiderNoLongerNew();
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
			String[] dateToCompare = sfd.format(compare).split("-");

			int numbOfDays = Period.between(LocalDate.of(Integer.parseInt(dateToCompare[0]), Integer.parseInt(dateToCompare[1]), Integer.parseInt(dateToCompare[2])), LocalDate.now()).getDays();
			if ((Integer.parseInt(dateToCompare[0]) == getThisYear()) && numbOfDays <= DAYS_TO_CONSIDER_NEW) return true;
		} catch (ParseException e) {
			NetworkExceptionHandler.handleException("isDomainNewlyCreated -> Parse", e);
		}
		return false;
	}

	private int getThisYear() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		String[] today = dtf.format(now).split("-"); // Year[0] Month[1] Day[2]
		return Integer.parseInt(today[0]);
	}

	/**
	 * Calculate the Date the Domain will no longer be considered as "NEW"
	 * @return The Date that we will not consider a Domain as New
	 */
	private Date getDateToConsiderNoLongerNew() throws ParseException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		String[] today = dtf.format(now).split("-"); // Year[0] Month[1] Day[2]

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date result;
		String[] dateToCompare = createdDate.split("-"); // Year[0] Month[1] Day[2]
		if (Integer.parseInt(today[2]) < DAYS_TO_CONSIDER_NEW) {
			int year = Integer.parseInt(dateToCompare[0]);
			int daysToRemove = Integer.parseInt(today[2]) - DAYS_TO_CONSIDER_NEW;
			int month = (Integer.parseInt(today[1]) - 1);
			int monthDays = getDaysInMonth(year, month);
			int newDay = monthDays + daysToRemove;
			result = sdf.parse((dateToCompare[0] + "-" + month + "-" + newDay));
		} else {
			result = sdf.parse((dateToCompare[0] + "-" + dateToCompare[1] + "-" + (Integer.parseInt(dateToCompare[2]) - DAYS_TO_CONSIDER_NEW)));
		}
		return result;
	}

	/**
	 * A method to grab the maximum days in a month
	 * @param year to help verify weather or not it's a leap year
	 * @param month The month to get the days from
	 * @return the total number of days in a month, taking leap year into consideration
	 */
	private int getDaysInMonth(int year, int month) {
		if (month == 2) return calcLeapYearForFeb(year) ? 29 : 28;
		else if ( month == 4 || month == 6 || month == 9 || month == 11 ) return 30;
		else return 31;
	}

	/**
	 * A leap year is exactly divisible by 4 except for century years (years ending with 00).
	 * The century year is a leap year only if it is perfectly divisible by 400.
	 * @param year the year to check
	 * @return if it is or is not a leap year
	 */
	private boolean calcLeapYearForFeb(int year) {
		// if the year is divided by 4
		if (year % 4 == 0) {
			// if the year is century
			if (year % 100 == 0) {
				// if year is divided by 400
				// then it is a leap year
				return year % 400 == 0;
			}
			return false;
		}
		return false;
	}

	@Override
	public String toString() {
		return "\ndomainName='" + domainName + '\''
				+ "\nupdatedDate='" + updatedDate + '\''
				+ "\ncreatedDate='" + createdDate + '\''
				+ "\nregistryExpiryDate='" + registryExpiryDate + '\''
				+ "\nregistrar='" + registrar + '\''
				+ "\nnameServers=" + nameServers
				+ "\nhasMailServer=" + hasMailServer
				+ "\nisNewlyRegistered=" + newlyRegistered
				+ "\nisInFamily=" + isInFamily;
	}
}
