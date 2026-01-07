package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class BOOLEAN extends RegexValue {
	private static final Pattern P_BOOLEAN = Pattern.compile("^(true|false)$");
	public static final BOOLEAN INSTANCE = new BOOLEAN();

	private BOOLEAN() {
		super(P_BOOLEAN, "Invalid boolean");
	}
	
    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }    
}
