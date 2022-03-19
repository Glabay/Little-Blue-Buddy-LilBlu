package xyz.glabaystudios.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import xyz.glabaystudios.web.LilBlu;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TimeZoneWrapper {

	static {
		getTimeZonePhoneBook();
	}

	private static ArrayList<String> timeZonePhoneBook;

	private static ArrayList<String> getTimeZonePhoneBook() {
		if (timeZonePhoneBook == null) timeZonePhoneBook = loadTimeZoneList();
		return timeZonePhoneBook;
	}

	public static HashMap<String, String> loadTimeZoneCsv() {
		HashMap<String, String> countries = new HashMap<>();
		try {
			CSVReader reader = new CSVReader(new FileReader(Objects.requireNonNull(LilBlu.class.getResource("country.csv")).getPath().replace("%20", " ")));
			String[] codeAndName;
			while((codeAndName = reader.readNext()) != null) {
				countries.put(codeAndName[0], codeAndName[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CsvValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return countries;
	}

	private static ArrayList<String> loadTimeZoneList() {
		HashMap<String, String> tempResultMap = new HashMap<>();
		ArrayList<String> timeZonePhoneBook = new ArrayList<>();
		try {
			CSVReader reader = new CSVReader(new FileReader(Objects.requireNonNull(LilBlu.class.getResource("time_zone.csv")).getPath().replace("%20", " ")));
			String[] line;
			boolean daylightSavings = TimeZone.getDefault().inDaylightTime(new Date());
			while((line = reader.readNext()) != null) {
				if (line[5].contains(daylightSavings ? "1" : "0")) {
					String toAdd = String.format("%s-%S-%S", line[0], line[1], line[2]);
					if (!tempResultMap.containsKey(line[2])) {
						tempResultMap.put(line[2], Arrays.toString(line));
						timeZonePhoneBook.add(toAdd);
					}
				}
			}
		} catch (CsvValidationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return timeZonePhoneBook;
	}

	public static ArrayList<String> getTimeZonePhoneBook(String countryCode) {
		ArrayList<String> temp = new ArrayList<>();
		getTimeZonePhoneBook().stream().map(entry -> entry.split("-")).forEach(splitLine -> {
			String continentalRegion = splitLine[0].trim();
			String timezoneCode = splitLine[2];
			TimeZone tz = TimeZone.getTimeZone(continentalRegion);
			String str = tz.getDisplayName(TimeZone.getDefault().inDaylightTime(new Date()), TimeZone.LONG);
			if (splitLine[1].equals(countryCode)) {
				String display = String.format("%S | %S", timezoneCode, str);
				temp.add(display);
			}
			if (countryCode.equals("``")) {
				String display = String.format("%S | %S", timezoneCode, str);
				temp.add(display);
			}
		});

		HashMap<String, String> tempResultMap = new HashMap<>();
		ArrayList<String> result = new ArrayList<>();

		temp.forEach(entry -> {
			String tz = entry.split("\\|")[0];
			if (!tempResultMap.containsKey(tz)) {
				tempResultMap.put(tz, entry);
				result.add(entry);
			}
		});

		Collections.sort(result);

		return result;
	}
}
