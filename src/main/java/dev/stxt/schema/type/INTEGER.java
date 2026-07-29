package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** Tipo {@code INTEGER}: valida un entero con signo opcional. */
public final class INTEGER extends RegexValue {
	private static final Pattern P_INTEGER = Pattern.compile("^(\\-|\\+)?\\d+$");
	/** Instancia única de este tipo. */
	public static final INTEGER INSTANCE = new INTEGER();

	private INTEGER() {
		super(P_INTEGER, "Invalid integer");
	}
	
    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }   
}
