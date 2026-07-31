package dev.stxt.schema.type;

import java.util.regex.Pattern;

/** {@code EMAIL} type: checks that the value looks like an e-mail address. */
public final class EMAIL extends RegexValue {
	private static final String EMAIL_PATTERN = "^(?=.{1,256})(?=.{1,64}@.{1,255}$)(?=.{1,64}@.{1,63}\\..{1,63}$)[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	private static final Pattern P_EMAIL = Pattern.compile(EMAIL_PATTERN);

	/** Single instance of this type. */
	public static final EMAIL INSTANCE = new EMAIL();

	private EMAIL() {
		super(P_EMAIL, "Invalid email");
	}
	
    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
}
