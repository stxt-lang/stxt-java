package dev.stxt.processors;

import java.util.List;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;

/** Process hook invoked by the {@link dev.stxt.Parser} when each node is closed, to validate in streaming. */
public interface Validator {
	/**
	 * Validates a node and returns every error found (without throwing), letting the caller
	 * collect errors from several nodes instead of bailing out on the first one. An empty list
	 * means the node is valid.
	 *
	 * @param n already closed node to validate.
	 * @return the validation errors found, or an empty list if the node is valid.
	 */
	List<ValidationException> validate(Node n);
}
