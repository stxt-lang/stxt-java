package dev.stxt.schema;

import java.io.IOException;
import java.util.List;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.resources.NotFoundException;
import dev.stxt.resources.ResourcesLoader;

public final class DocumentValidator {
	private final SchemaValidator validator;

	public DocumentValidator(ResourcesLoader resourcesLoader) {
		validator = new SchemaValidator(new SchemaProviderCache(resourcesLoader));
	}

	public Node validateNode(Node node) throws ParseException, IOException, NotFoundException {
		return validator.validateNode(node);
	}

	public List<Node> validateNodes(List<Node> nodes) throws ParseException, IOException, NotFoundException {
		return validator.validateNodes(nodes);
	}
}
