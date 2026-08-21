package dev.stxt.schema.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/**
 * A type whose value must match a regular expression and whose captured groups must then pass a
 * range check (the calendar and clock types of STXT-SCHEMA-SPEC 9.4). INLINE value form only.
 */
abstract class RangeValue implements Type {
	private final Pattern pattern;
	private final String error;

	protected RangeValue(Pattern pattern, String error) {
		this.pattern = pattern;
		this.error = error;
	}

	/** True if the captured groups of a matching value are in range. */
	protected abstract boolean inRange(Matcher m);

	@Override
	public void validate(NodeDefinition ndef, Node n) {
		// INLINE value form (STXT-SCHEMA-SPEC 9.4): the '>>' block is not accepted
		if (n.isTextNode()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_TEXT",
					"Not allowed text in node " + n.getQualifiedName());
		}

		String value = n.getText();
		Matcher m = pattern.matcher(value);
		if (!m.matches() || !inRange(m)) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", n.getName() + ": " + error + " (" + value + ")");
		}
	}

	/** The integer of a captured group, or {@code fallback} when the group did not match. */
	protected static int group(Matcher m, int i, int fallback) {
		return m.group(i) == null ? fallback : Integer.parseInt(m.group(i));
	}
}
