package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class INLINE implements Type {
	public static final INLINE INSTANCE = new INLINE();

	private INLINE() {
	}

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n.getTextLines().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_TEXT",
					"Not allowed text in node " + n.getQualifiedName());
		}
	}

	@Override
	public String getName() {
		return "INLINE";
	}
}
