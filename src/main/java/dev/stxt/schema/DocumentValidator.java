package dev.stxt.schema;

import dev.stxt.IOException;
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

	public void validateNode(Node node) throws ParseException, IOException, NotFoundException {
		validator.validateNode(node);
	}

	public void validateNodes(List<Node> nodes) throws ParseException, IOException, NotFoundException {
		validator.validateNodes(nodes);
	}
}
