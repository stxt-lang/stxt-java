package dev.stxt.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.processors.Validator;

/** {@link Validator} que, por cada nodo, resuelve su {@link Schema} vía un {@link SchemaProvider} y valida tipo y cardinalidad. */
public class SchemaValidator implements Validator {
	private final SchemaProvider schemaProvider;
	private boolean recursiveValidation = false;

	/** @param schemaProvider de dónde resolver el schema de cada namespace. Sin validación recursiva de hijos. */
	public SchemaValidator(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}
	/**
	 * @param schemaProvider de dónde resolver el schema de cada namespace.
	 * @param recursive si valida también recursivamente los hijos de cada nodo.
	 */
	public SchemaValidator(SchemaProvider schemaProvider, boolean recursive) {
		this.schemaProvider = schemaProvider;
		this.recursiveValidation = recursive;
	}

	@Override
	public List<ValidationException> validate(Node node) {
		List<ValidationException> errors = new ArrayList<>();

		// Obtenemos namespace
		String namespace = node.getNamespace();
		Schema sch = schemaProvider.getSchema(namespace);
		if (sch == null) {
			errors.add(new ValidationException(node.getLine(), "SCHEMA_NOT_FOUND", "Not found schema: " + namespace));
			return errors;
		}

		// Validamos nodo
		errors.addAll(validateAgainstSchema(node, sch));

		// Validamos children
		if (recursiveValidation)
			for (Node n: node.getChildren())
				errors.addAll(validate(n));

		return errors;
	}
	
	/**
	 * Valida un nodo contra un schema ya resuelto: existencia, tipo de valor y cardinalidades de sus hijos.
	 *
	 * @param node nodo a validar.
	 * @param sch schema contra el que validar.
	 * @return los errores de validación encontrados, vacío si el nodo es válido.
	 */
	public List<ValidationException> validateAgainstSchema(Node node, Schema sch) {
		List<ValidationException> errors = new ArrayList<>();

	    NodeDefinition schemaNode = sch.getNodeDefinition(node.getNormalizedName());
	    if (schemaNode == null) {
	        String error = "NOT EXIST NODE " + node.getNormalizedName() + " for namespace " + sch.getNamespace();
	        errors.add(new ValidationException(node.getLine(), "NODE_NOT_EXIST_IN_SCHEMA", error));
	        return errors;
	    }

	    errors.addAll(validateValue(schemaNode, node));
	    errors.addAll(validateChildrenDeclared(schemaNode, node));
	    errors.addAll(validateCount(schemaNode, node));

	    return errors;
	}

	// Modelo de contenido cerrado (STXT-SCHEMA-SPEC, sección 6): solo se permiten
	// los hijos directos declarados en la definición del padre; sin Children, cierre total
	private static List<ValidationException> validateChildrenDeclared(NodeDefinition nsNode, Node node) {
		List<ValidationException> errors = new ArrayList<>();

		for (Node child : node.getChildren()) {
			if (!nsNode.getChildren().containsKey(child.getQualifiedName()))
				errors.add(new ValidationException(child.getLine(), "CHILD_NOT_DECLARED",
						"Child '" + child.getQualifiedName() + "' not declared in node '" + node.getQualifiedName() + "'"));
		}

		return errors;
	}

	private static List<ValidationException> validateValue(NodeDefinition nsNode, Node n) {
		List<ValidationException> errors = new ArrayList<>();
		String nodeType = nsNode.getType();

		Type validator = TypeRegistry.get(nodeType);
		if (validator == null) {
			errors.add(new ValidationException(n.getLine(), "TYPE_NOT_SUPPORTED", "Node type not supported: " + nodeType));
			return errors;
		}

		try {
			validator.validate(nsNode, n);
		} catch (ValidationException e) {
			errors.add(e);
		} catch (RuntimeException e) {
			errors.add(new ValidationException(n.getLine(), "VALIDATION_ERROR", e.getMessage()));
		}

		return errors;
	}

	private static List<ValidationException> validateCount(NodeDefinition nsNode, Node node) {
		List<ValidationException> errors = new ArrayList<>();
		Map<String, Integer> count = new HashMap<>();

		for (Node child : node.getChildren()) {
			// Count childs
			String childName = child.getQualifiedName();
			count.put(childName, count.getOrDefault(childName, 0) + 1);
		}

		for (ChildDefinition chNode : nsNode.getChildren().values()) {
			errors.addAll(validateCount(chNode, count.getOrDefault(chNode.getQualifiedName(), 0), node));
		}

		return errors;
	}

	private static List<ValidationException> validateCount(ChildDefinition chNode, int num, Node node) {
		List<ValidationException> errors = new ArrayList<>();
		Integer min = chNode.getMin();
		Integer max = chNode.getMax();

		if (min != null && num < min)
			errors.add(new ValidationException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and min is " + min));

		if (max != null && num > max)
			errors.add(new ValidationException(node.getLine(), "INVALID_NUMBER",
					num + " nodes of '" + chNode.getQualifiedName() + " and max is " + max));

		return errors;
	}

}
