package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class DateType extends RegexValueType {
	private static final Pattern P_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
	public static final DateType INSTANCE = new DateType();

	private DateType() {
		super(P_DATE, "Invalid date");
	}

	@Override
	public String getName() {
		return "DATE";
	}
}
