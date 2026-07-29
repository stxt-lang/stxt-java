package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** Tipo {@code TIME}: valida formato {@code HH:MM:SS}. */
public final class TIME extends RegexValue {
	private static final Pattern P_TIME = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
	/** Instancia única de este tipo. */
	public static final TIME INSTANCE = new TIME();

	private TIME() {
		super(P_TIME, "Invalid time");
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
}
