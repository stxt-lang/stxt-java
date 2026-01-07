package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;
import dev.stxt.utils.StringUtils;

public final class BASE64 implements Type {
	public static final BASE64 INSTANCE = new BASE64();

	private BASE64() {
	}

	@Override
	public void validate(NodeDefinition ndef, Node n) {
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
