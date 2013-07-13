package common;

import java.util.Date;

public class DateUtils {
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
}