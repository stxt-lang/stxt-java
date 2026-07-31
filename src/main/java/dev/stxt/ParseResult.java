package dev.stxt;

import java.util.ArrayList;
import java.util.List;

import dev.stxt.exceptions.ParseException;

/**
 * Result of a parse in multi-error mode: it collects the root nodes obtained and every error
 * found (both syntax and validation ones), without bailing out on the first one.
 *
 * See {@link Parser#parseResult(String)}. For the traditional fail-fast mode use
 * {@link Parser#parse(String)}, which internally uses this result and throws the first error.
 */
public class ParseResult {
	private final List<Node> nodes = new ArrayList<>();
	private final List<ParseException> errors = new ArrayList<>();

	/** Creates an empty result, with no nodes and no errors. */
	public ParseResult() {
	}

	/** {@return the root nodes collected so far} */
	public List<Node> getNodes() {
		return nodes;
	}

	/** {@return the syntax or validation errors collected so far, in order of appearance} */
	public List<ParseException> getErrors() {
		return errors;
	}

	/** {@return {@code true} if at least one error has been collected} */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	/**
	 * Adds a root node to the result.
	 *
	 * @param node already closed root node to add to the result.
	 */
	public void addNode(Node node) {
		nodes.add(node);
	}

	/**
	 * Adds an error found while parsing.
	 *
	 * @param error error found while parsing, without aborting the traversal.
	 */
	public void addError(ParseException error) {
		errors.add(error);
	}
}
