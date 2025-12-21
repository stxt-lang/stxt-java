package dev.stxt.schema;

import java.util.Collections;
import java.util.List;

import dev.stxt.Node;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.validator.TopDownValidator;

public final class SchemaValidatorService {
	private final TopDownValidator validator;

	public SchemaValidatorService(ResourcesLoader resourcesLoader) {
		validator = new TopDownValidator(Collections.singletonList(new SchemaValidator(new SchemaProviderCache(resourcesLoader))));
	}

	public void validateNode(Node node) {
		validator.validate(node);
	}

	public void validateNodes(List<Node> nodes) {
		for (Node n: nodes)
			validator.validate(n);
	}
}
