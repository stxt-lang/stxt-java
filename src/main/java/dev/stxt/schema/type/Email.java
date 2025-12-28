package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class Email extends RegexValue {
	private static final String EMAIL_PATTERN = "^(?=.{1,256})(?=.{1,64}@.{1,255}$)(?=.{1,64}@.{1,63}\\..{1,63}$)[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	private static final Pattern P_EMAIL = Pattern.compile(EMAIL_PATTERN);

	public static final Email INSTANCE = new Email();

	private Email() {
		super(P_EMAIL, "Invalid email");
	}

	@Override
	public String getName() {
		return "EMAIL";
	}
}
