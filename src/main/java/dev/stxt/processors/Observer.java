package dev.stxt.processors;

import dev.stxt.Node;

/** Process hook notified by the {@link dev.stxt.Parser} when each node is opened and closed. */
public interface Observer {
	/** @param node node just opened (its children and value are not complete yet). */
	void onCreate(Node node);
	/** @param node node just closed, with all its children and its value already complete. */
	void onFinish(Node node);
}
