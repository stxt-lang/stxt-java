package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class NATURAL extends RegexValue {
	private static final Pattern P_NATURAL = Pattern.compile("^\\d+$");
	public static final NATURAL INSTANCE = new NATURAL();

	private NATURAL() {
		super(P_NATURAL, "Invalid natural");
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }   
}
