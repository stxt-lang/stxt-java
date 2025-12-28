package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class TimestampType extends RegexValueType {
	private static final String ISO_8601_PATTERN = "^\\d{4}-\\d{2}-\\d{2}" + "T" + "\\d{2}:\\d{2}(:\\d{2}(\\.\\d{3})?)?"
			+ "(Z|[+-]\\d{2}:\\d{2})?$";
	private static final Pattern P_TIMESTAMP = Pattern.compile(ISO_8601_PATTERN);

	public static final TimestampType INSTANCE = new TimestampType();

	private TimestampType() {
		super(P_TIMESTAMP, "Invalid timestamp");
	}

	@Override
	public String getName() {
		return "TIMESTAMP";
	}
}
