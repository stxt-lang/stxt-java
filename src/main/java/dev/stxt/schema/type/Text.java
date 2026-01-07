package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class Text implements Type {
	public static final Text INSTANCE = new Text();

	private Text() {
	}

	@Override
    public void validate(NodeDefinition ndef, Node n) {
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
