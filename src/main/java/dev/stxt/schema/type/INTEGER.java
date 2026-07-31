package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** {@code INTEGER} type: checks an integer with an optional sign. */
public final class INTEGER extends RegexValue {
	private static final Pattern P_INTEGER = Pattern.compile("^(\\-|\\+)?\\d+$");
	/** Single instance of this type. */
	public static final INTEGER INSTANCE = new INTEGER();

	private INTEGER() {
		super(P_INTEGER, "Invalid integer");
	}
	
    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }   
}
