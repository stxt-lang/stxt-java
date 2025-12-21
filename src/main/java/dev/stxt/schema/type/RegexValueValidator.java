package dev.stxt.schema.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.schema.TypeValidator;

/**
 * Base validator for simple regex-based value checks.
 */
abstract class RegexValueValidator implements TypeValidator {
	private final Pattern pattern;
	private final String error;

	protected RegexValueValidator(Pattern pattern, String error) {
		this.pattern = pattern;
		this.error = error;
	}

	@Override
	public void validate(Node n) {
		String value = n.getText();
		Matcher m = pattern.matcher(value);
		if (!m.matches()) {
			throw new ParseException(n.getLine(), "INVALID_VALUE", n.getName() + ": " + error + " (" + value + ")");
		}
	}
}
