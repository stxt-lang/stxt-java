package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class Date extends RegexValue {
	private static final Pattern P_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
	public static final Date INSTANCE = new Date();

	private Date() {
		super(P_DATE, "Invalid date");
	}

	@Override
	public String getName() {
		return "DATE";
	}
}
