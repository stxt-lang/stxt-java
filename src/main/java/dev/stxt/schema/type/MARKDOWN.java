package dev.stxt.schema.type;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/**
 * STXT-SCHEMA-SPEC 9.7: a efectos de validación equivale a TEXT (cualquier
 * contenido es Markdown válido); sólo se prohíben hijos.
 */
public final class MARKDOWN implements Type {
	/** Instancia única de este tipo. */
	public static final MARKDOWN INSTANCE = new MARKDOWN();

	private MARKDOWN() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }

	@Override
    public void validate(NodeDefinition ndef, Node n) {
		if (n.getChildren().size() > 0) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_CHILDREN_TEXT",
					"Not allowed children nodes in node " + n.getQualifiedName());
		}
	}
}
