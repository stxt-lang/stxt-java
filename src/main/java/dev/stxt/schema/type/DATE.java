package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** Tipo {@code DATE}: valida formato {@code YYYY-MM-DD}. */
public final class DATE extends RegexValue {
	private static final Pattern P_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
	/** Instancia única de este tipo. */
	public static final DATE INSTANCE = new DATE();

	private DATE() {
		super(P_DATE, "Invalid date");
	}

	@Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }    
}
