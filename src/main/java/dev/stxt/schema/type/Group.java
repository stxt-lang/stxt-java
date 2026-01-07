package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class Group implements Type {
	public static final Group INSTANCE = new Group();

	private Group() {
	}

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (!n.getValue().isEmpty() || n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' has to be empty");
		}
	}

	@Override
	public String getName() {
		return "GROUP";
	}
}
