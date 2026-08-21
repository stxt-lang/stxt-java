package dev.stxt.schema.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code TIMESTAMP} type: {@code DATE "T" hh:mm [":" ss ["." digits]] ["Z" | sign hh:mm]}
 * (STXT-SCHEMA-SPEC 9.4). Date, time and offset in range; seconds, fraction (one or more digits)
 * and zone optional.
 */
public final class TIMESTAMP extends RangeValue {
	private static final Pattern P_TIMESTAMP = Pattern.compile(
			"^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})(?::(\\d{2})(?:\\.\\d+)?)?(?:Z|[+-](\\d{2}):(\\d{2}))?$");

	/** Single instance of this type. */
	public static final TIMESTAMP INSTANCE = new TIMESTAMP();

	private TIMESTAMP() {
		super(P_TIMESTAMP, "Invalid timestamp");
	}

	@Override
	protected boolean inRange(Matcher m) {
		return DateTime.isValidDate(group(m, 1, 0), group(m, 2, 0), group(m, 3, 0))
				&& DateTime.isValidTime(group(m, 4, 0), group(m, 5, 0), group(m, 6, 0))
				&& (m.group(7) == null || DateTime.isValidTime(group(m, 7, 0), group(m, 8, 0), 0));
	}

	@Override
	public String getName() {
		return "TIMESTAMP";
	}
}
