package dev.stxt.schema.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** {@code TIME} type: {@code hh:mm:ss} in range (00-23, 00-59, 00-59); no fraction, no zone (STXT-SCHEMA-SPEC 9.4). */
public final class TIME extends RangeValue {
	private static final Pattern P_TIME = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$");

	/** Single instance of this type. */
	public static final TIME INSTANCE = new TIME();

	private TIME() {
		super(P_TIME, "Invalid time");
	}

	@Override
	protected boolean inRange(Matcher m) {
		return DateTime.isValidTime(group(m, 1, 0), group(m, 2, 0), group(m, 3, 0));
	}

}
