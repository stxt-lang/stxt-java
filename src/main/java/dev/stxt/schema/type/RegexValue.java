package dev.stxt.schema.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/**
 * Base validator for simple regex-based value checks.
 */
abstract class RegexValue implements Type {
	private final Pattern pattern;
	private final String error;

	protected RegexValue(Pattern pattern, String error) {
		this.pattern = pattern;
		this.error = error;
	}

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// INLINE value form (STXT-SCHEMA-SPEC 9.3/9.4): the '>>' block is not accepted
		if (n.isTextNode()) {
			throw new ValidationException(n.getLine(), "BLOCK_FORM_NOT_ALLOWED",
					"Not allowed text in node " + n.getQualifiedName());
		}

		String value = n.getText();
		Matcher m = pattern.matcher(value);
		if (!m.matches()) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", n.getName() + ": " + error + " (" + value + ")");
		}
	}
}
