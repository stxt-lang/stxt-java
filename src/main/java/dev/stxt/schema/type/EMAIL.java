package dev.stxt.schema.type;

import java.util.regex.Pattern;

/**
 * {@code EMAIL} type: checks that the value is an e-mail address, per the normative grammar of
 * STXT-SCHEMA-SPEC 9.4, in either of its two forms: the bare address ({@code user@domain.tld}) or
 * a display name followed by the address between angle brackets
 * ({@code Joan Costa <joan@example.com>}). The display name is any non-empty text without
 * {@code <} or {@code >} (quotes are not interpreted) whose last character is not a blank, and
 * the blank before {@code <} is optional; {@code <}/{@code >} without a name, unbalanced or
 * followed by anything are rejected. ASCII only (no EAI), permissive with dots (the full RFC 5322
 * dot-atom is not replicated), RFC 5321 practical length limits: local part 1-64, whole address at
 * most 254, TLD 2-63 letters. Blanks are the STXT ones (U+0020/U+0009) only, so no {@code \s} here.
 */
public final class EMAIL extends RegexValue {
	/** The address proper, {@code local@domain} per the normative grammar, as it reads when it ends the value... */
	private static final String ADDRESS = "(?=.{1,254}$)(?=[^@]{1,64}@)"
			+ "[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}";
	/** ...and the same address as it reads between {@code <} and {@code >} (the lookaheads stop at the {@code >}). */
	private static final String BRACKETED = "(?=[^>]{1,254}>$)(?=[^@>]{1,64}@)"
			+ "[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}";
	private static final Pattern P_EMAIL = Pattern.compile("^(?:[^<>]*[^<> \\t][ \\t]*<" + BRACKETED + ">|" + ADDRESS + ")$");

	/** Single instance of this type. */
	public static final EMAIL INSTANCE = new EMAIL();

	private EMAIL() {
		super(P_EMAIL, "Invalid email");
	}
	
}
