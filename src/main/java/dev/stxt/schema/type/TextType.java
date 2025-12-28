package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.processors.ValidationException;
import dev.stxt.schema.Type;

public final class TextType implements Type {
	public static final TextType INSTANCE = new TextType();

	private TextType() {
	}

	@Override
	public void validate(Node n) {
		if (n.getChildren().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_CHILDREN_TEXT",
					"Not allowed children nodes in node " + n.getQualifiedName());
		}
	}

	@Override
	public String getName() {
		return "TEXT";
	}
}
