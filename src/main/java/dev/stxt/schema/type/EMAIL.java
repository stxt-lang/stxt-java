package dev.stxt.schema.type;

import java.util.regex.Pattern;

/**
 * {@code EMAIL} type: checks that the value is an e-mail address, in either of the two forms of
 * STXT-SCHEMA-SPEC 9.4: the bare address ({@code user@domain.tld}) or a display name followed by
 * the address between angle brackets ({@code Joan Costa <joan@example.com>}). The display name is
 * any non-empty text without {@code <} or {@code >} (quotes are not interpreted) and the space
 * before {@code <} is optional; {@code <}/{@code >} without a name, unbalanced or followed by
 * anything are rejected.
 */
public final class EMAIL extends RegexValue {
	/** The address proper, {@code local@domain} with the usual length limits, as it reads when it ends the value... */
	private static final String ADDRESS = "(?=.{1,256}$)(?=.{1,64}@.{1,255}$)(?=.{1,64}@.{1,63}\\..{1,63}$)"
			+ "[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
	/** ...and the same address as it reads between {@code <} and {@code >} (the lookaheads stop at the {@code >}). */
	private static final String BRACKETED = "(?=[^>]{1,256}>$)(?=[^>]{1,64}@[^>]{1,255}>$)(?=[^>]{1,64}@[^>]{1,63}\\.[^>]{1,63}>$)"
			+ "[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
	private static final Pattern P_EMAIL = Pattern.compile("^(?:[^<>]*[^<>\\s]\\s*<" + BRACKETED + ">|" + ADDRESS + ")$");

	/** Single instance of this type. */
	public static final EMAIL INSTANCE = new EMAIL();

	private EMAIL() {
		super(P_EMAIL, "Invalid email");
	}
	
}
