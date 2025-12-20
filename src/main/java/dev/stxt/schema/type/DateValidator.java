package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class DateValidator extends RegexValueValidator {
	private static final Pattern P_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
	public static final DateValidator INSTANCE = new DateValidator();

	private DateValidator() {
		super(P_DATE, "Invalid date");
	}
}
