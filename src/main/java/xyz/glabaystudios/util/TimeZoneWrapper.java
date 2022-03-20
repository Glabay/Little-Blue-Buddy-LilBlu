package xyz.glabaystudios.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TimeZoneWrapper {

	private static ArrayList<String> timeZonePhoneBook;

	public static TimeZoneWrapper timeZoneWrapper;

	public static TimeZoneWrapper getWrapper() {
		if (timeZoneWrapper == null) timeZoneWrapper = new TimeZoneWrapper();
		if (timeZonePhoneBook == null) timeZonePhoneBook = timeZoneWrapper.loadTimeZoneList();
		return timeZoneWrapper;
	}

	private ArrayList<String> getTimeZonePhoneBook() {
		if (timeZonePhoneBook == null) timeZonePhoneBook = loadTimeZoneList();
		return timeZonePhoneBook;
	}

	public HashMap<String, String> loadTimeZoneCsv() {
		HashMap<String, String> countries = new HashMap<>();
		try {
			File csv = new File("./country.csv");
			FileReader csvFile = new FileReader(csv);
			CSVReader reader = new CSVReader(csvFile);
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

	private ArrayList<String> loadTimeZoneList() {
		HashMap<String, String> tempResultMap = new HashMap<>();
		ArrayList<String> timeZonePhoneBook = new ArrayList<>();
		try {
			CSVReader reader = new CSVReader(new FileReader("./time_zone.csv"));
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CsvValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return timeZonePhoneBook;
	}

	public ArrayList<String> getTimeZonePhoneBook(String countryCode) {
		ArrayList<String> temp = new ArrayList<>();
		getTimeZonePhoneBook().stream().map(entry -> entry.split("-")).forEach(splitLine -> {
			String continentalRegion = splitLine[0].trim();
			String timezoneCode = splitLine[2];
			TimeZone tz = TimeZone.getTimeZone(continentalRegion);
			String str = tz.getDisplayName(TimeZone.getDefault().inDaylightTime(new Date()), TimeZone.LONG);
			if (splitLine[1].equals(countryCode)) {
				String display = String.format("%S | %s", timezoneCode, str);
				temp.add(display);
			}
			if (countryCode.equals("``")) {
				String display = String.format("%S | %s", timezoneCode, str);
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
