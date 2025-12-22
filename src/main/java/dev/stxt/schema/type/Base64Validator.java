package dev.stxt.schema.type;

import java.util.Base64;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.TypeValidator;
import dev.stxt.utils.StringUtils;

public final class Base64Validator implements TypeValidator {
	public static final Base64Validator INSTANCE = new Base64Validator();

	private Base64Validator() {
	}

	@Override
	public void validate(Node n) {
		try {
			Base64.getDecoder().decode(StringUtils.cleanupString(n.getText()));
		} catch (Exception e) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' Invalid Base64");
		}
	}
}
