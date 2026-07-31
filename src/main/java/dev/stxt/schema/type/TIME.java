package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** {@code TIME} type: checks the {@code HH:MM:SS} format. */
public final class TIME extends RegexValue {
	private static final Pattern P_TIME = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
	/** Single instance of this type. */
	public static final TIME INSTANCE = new TIME();

	private TIME() {
		super(P_TIME, "Invalid time");
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
}
