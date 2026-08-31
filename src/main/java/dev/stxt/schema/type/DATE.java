package dev.stxt.schema.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** {@code DATE} type: {@code YYYY-MM-DD}, an existing date of the proleptic Gregorian calendar (STXT-SCHEMA-SPEC 9.4). */
public final class DATE extends RangeValue {
	private static final Pattern P_DATE = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");

	/** Single instance of this type. */
	public static final DATE INSTANCE = new DATE();

	private DATE() {
		super(P_DATE, "Invalid date");
	}

	@Override
	protected boolean inRange(Matcher m) {
		return DateTime.isValidDate(group(m, 1, 0), group(m, 2, 0), group(m, 3, 0));
	}

}
