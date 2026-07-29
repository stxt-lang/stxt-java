package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** Tipo {@code BOOLEAN}: valida que el valor sea {@code true} o {@code false}. */
public final class BOOLEAN extends RegexValue {
	private static final Pattern P_BOOLEAN = Pattern.compile("^(true|false)$");
	/** Instancia única de este tipo. */
	public static final BOOLEAN INSTANCE = new BOOLEAN();

	private BOOLEAN() {
		super(P_BOOLEAN, "Invalid boolean");
	}
	
    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }    
}
