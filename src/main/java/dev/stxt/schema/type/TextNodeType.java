package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.Type;

public final class TextNodeType implements Type {
	public static final TextNodeType INSTANCE = new TextNodeType();

	private TextNodeType() {
	}

	@Override
	public String getName() {
		return "TEXT_NODE";
	}
	
	@Override
	public void validate(Node n) {
		if (!n.getValue().isEmpty()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_VALUE",
					"Not allowed inline text in node " + n.getQualifiedName());
		}
	}

}
