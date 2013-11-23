package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	private static final SimpleDateFormat[] RFC_0822_DATE_FORMATS = new SimpleDateFormat[]{
		new SimpleDateFormat("E, dd MMM yyyy kk:mm:ss z"),
		new SimpleDateFormat("E, dd MMM yyyy kk:mm:ss Z"),
		new SimpleDateFormat("dd MMM yyyy kk:mm:ss z"),
		new SimpleDateFormat("dd MMM yyyy kk:mm:ss Z")
	};
	
	private DateUtils() {
	}
	
	/**
	 * Converts the millis to a date. If the value is less than
	 * 0 then a null is returned.
	 */
	public static Date toDate(long millis) {
		return (millis < 0) ? null : new Date(millis);
	}
	
	/**
	 * Converts the date to millis. If the date is null, then 
	 * -1 is returned.
	 */
	public static long toMillis(Date date) {
		return (date == null) ? -1 : date.getTime();
	}
	
	/**
	 * Parses a string containing an RFC 0822 date 
	 */
	public static Date parseRfc0822Date(String value) throws ParseException {
		Date date = null;
		for(SimpleDateFormat dateFormat : RFC_0822_DATE_FORMATS) {
			try {
				date = dateFormat.parse(value);
				return date;
			} catch(ParseException exc) {
				//silently swallow the exception so that we can try the next format
			}
		}
		
		throw new ParseException("Date is not in a valid RFC822 format: " + value, 0);
	}
}