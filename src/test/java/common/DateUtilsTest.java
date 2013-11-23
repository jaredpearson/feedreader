package common;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import common.DateUtils;

public class DateUtilsTest {
	
	@Test
	public void testToMillis() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.MONTH, 7); //august
		calendar.set(Calendar.DATE, 10);
		calendar.set(Calendar.YEAR, 2013);
		
		long actual = DateUtils.toMillis(calendar.getTime());
		
		assertEquals(1376118000000l, actual);
	}

	@Test
	public void testToMillisWithNull() {
		long actual = DateUtils.toMillis(null);
		assertEquals(-1l, actual);
	}
	
	@Test
	public void testToDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.MONTH, 7); //august
		calendar.set(Calendar.DATE, 10);
		calendar.set(Calendar.YEAR, 2013);
		
		Date actual = DateUtils.toDate(1376118000000l);
		
		assertTrue(calendar.getTime().equals(actual));
	}
	
	@Test
	public void testToDateWithNull() {
		Date actual = DateUtils.toDate(-1l);
		assertTrue(actual == null);
	}

	@Test(expected=ParseException.class)
	public void testParseRfc0822WithInvalid() throws Exception {
		Date date = DateUtils.parseRfc0822Date("not a date");
		assertEquals(1055238061000l, date.getTime());
	}
	
	@Test
	public void testParseRfc0822WithDay() throws Exception {
		Date date = DateUtils.parseRfc0822Date("Tue, 10 Jun 2003 09:41:01 GMT");
		assertEquals(1055238061000l, date.getTime());
	}
	
	@Test
	public void testParseRfc0822WithoutDay() throws Exception {
		Date date = DateUtils.parseRfc0822Date("10 Jun 2003 09:41:01 GMT");
		assertEquals(1055238061000l, date.getTime());
	}

	@Test
	public void testParseRfc0822WithDayWithRfc822TimeZone() throws Exception {
		Date date = DateUtils.parseRfc0822Date("Tue, 10 Jun 2003 09:41:01 -0800");
		assertEquals(1055266861000l, date.getTime());
	}

	@Test
	public void testParseRfc0822WithoutDayWithRfc822TimeZone() throws Exception {
		Date date = DateUtils.parseRfc0822Date("10 Jun 2003 09:41:01 -0800");
		assertEquals(1055266861000l, date.getTime());
	}
}
