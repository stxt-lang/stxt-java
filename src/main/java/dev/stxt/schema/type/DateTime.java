package dev.stxt.schema.type;

/**
 * Calendar and clock ranges shared by {@code DATE}, {@code TIME} and {@code TIMESTAMP}
 * (STXT-SCHEMA-SPEC 9.4): the shape of a value is checked by each type's regular expression, the
 * ranges by these helpers. Never {@code java.time}, whose lenient parsers differ from port to port.
 */
final class DateTime {
	private DateTime() {
	}

	/** True if the year-month-day exists in the proleptic Gregorian calendar (year 0000-9999). */
	static boolean isValidDate(int year, int month, int day) {
		if (month < 1 || month > 12 || day < 1) {
			return false;
		}
		boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
		int[] daysInMonth = { 31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		return day <= daysInMonth[month - 1];
	}

	/** True if hour 00-23, minute 00-59, second 00-59 (no leap second). */
	static boolean isValidTime(int hour, int minute, int second) {
		return hour <= 23 && minute <= 59 && second <= 59;
	}
}
