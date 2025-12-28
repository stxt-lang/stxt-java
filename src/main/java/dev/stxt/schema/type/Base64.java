package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.Type;
import dev.stxt.utils.StringUtils;

public final class Base64 implements Type {
	public static final Base64 INSTANCE = new Base64();

	private Base64() {
	}

	@Override
	public void validate(Node n) {
		try {
			java.util.Base64.getDecoder().decode(StringUtils.cleanSpaces(n.getText()));
		} catch (Exception e) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' Invalid Base64");
		}
	}

	@Override
	public String getName() {
		return "BASE64";
	}
}
