package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.schema.TypeValidator;

public final class TextValidator implements TypeValidator {
	public static final TextValidator INSTANCE = new TextValidator();

	private TextValidator() {
	}

	@Override
	public void validate(Node n) throws ParseException {
		if (n.getChildren() != null && n.getChildren().size() > 0) {
			throw new ParseException(n.getLine(), "NOT_ALLOWED_CHILDREN_TEXT",
					"Not allowed children nodes in node " + n.getQualifiedName());
		}
	}
}
