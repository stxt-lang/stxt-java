package dev.stxt.processors;

import dev.stxt.Node;
import dev.stxt.exceptions.ParseException;

/**
 * Process hook notified by the {@link dev.stxt.Parser} with the stream of results a parse
 * emits: each completed root node, and every error. It complements {@link Observer}, which
 * watches the process line by line: a StreamObserver only sees finished roots and errors, so a
 * consumer that processes a document root by root never has to ask a node for its level.
 * Register it with {@link dev.stxt.Parser#registerStreamObserver(StreamObserver)}; a class may
 * implement {@link Observer}, StreamObserver or both.
 *
 * <p>It fires in every entry point — {@code parse()}, {@code parseResult()} and
 * {@code parseStream()} — the same way; what {@code parseStream()} adds is that the parser
 * retains nothing, so there these callbacks are the only way to get the results.
 * StreamObservers must not modify the nodes they receive.
 */
public interface StreamObserver {
	/**
	 * Called when a root (level 0) node is closed, with its whole subtree already complete —
	 * children, values, text lines — and its validators already run. In
	 * {@link dev.stxt.Parser#parseStream(Iterable)} the parser releases the node right after
	 * this call, so the memory in use is one root tree at a time.
	 *
	 * @param node the completed root node.
	 */
	void onRootNode(Node node);

	/**
	 * Called for every error found (syntax or validation), in order of appearance. Parsing
	 * continues with the next line, except for {@code LIMIT_*} errors
	 * ({@link dev.stxt.exceptions.LimitException}, STXT-SPEC 11.2), which abort the parse right
	 * after this call. In fail-fast {@link dev.stxt.Parser#parse(String)}, which stops at the
	 * first error, the observer sees that error before it is thrown.
	 *
	 * @param error the error found.
	 */
	void onError(ParseException error);
}
