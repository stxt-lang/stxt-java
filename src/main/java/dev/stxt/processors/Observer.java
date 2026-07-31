package dev.stxt.processors;

import dev.stxt.Node;

/** Process hook notified by the {@link dev.stxt.Parser} when each node is opened and closed. */
public interface Observer {
	/**
	 * Called when a node is opened.
	 *
	 * @param node node just opened (its children and value are not complete yet).
	 */
	void onCreate(Node node);
	/**
	 * Called when a node is closed.
	 *
	 * @param node node just closed, with all its children and its value already complete.
	 */
	void onFinish(Node node);
}
