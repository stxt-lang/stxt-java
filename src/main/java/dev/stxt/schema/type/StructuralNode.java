package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Type;

public final class StructuralNode implements Type {
	public static final StructuralNode INSTANCE = new StructuralNode();

	private StructuralNode() {
	}

	@Override
	public void validate(Node n) {
		if (!n.getValue().isEmpty() || n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' has to be empty");
		}
	}

	@Override
	public String getName() {
		return "STRUCTURAL_NODE";
	}
}
