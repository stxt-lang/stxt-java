package dev.stxt.processors;

import dev.stxt.LineIndent;
import dev.stxt.Node;
import dev.stxt.TextNode;

/**
 * Process hook notified by the {@link dev.stxt.Parser} while parsing: when each node is opened
 * and closed, and for every comment and text line it reads. Register it with
 * {@link dev.stxt.Parser#registerObserver(Observer)}. Observers must not modify the nodes they
 * receive.
 */
public interface Observer {
	/**
	 * Called when a node is opened. The node is already attached to its parent, so its effective
	 * namespace and its level are available.
	 *
	 * @param node node just opened (its children and its text lines are not complete yet).
	 * @param line source line that opened the node, as it appears in the document.
	 */
	void onCreate(Node node, String line);
	/**
	 * Called when a node is closed.
	 *
	 * @param node node just closed, with all its children and its value already complete.
	 */
	void onFinish(Node node);
	/**
	 * Called for every comment line, which produces no node.
	 *
	 * @param lineNumber line number of the comment.
	 * @param line source line of the comment, as it appears in the document.
	 */
	void onComment(int lineNumber, String line);
	/**
	 * Called for every text line appended to an open BLOCK node.
	 *
	 * @param node BLOCK node the line was appended to.
	 * @param lineNumber line number of the text line.
	 * @param lineString source line, as it appears in the document.
	 * @param line the same line already split into indentation and content.
	 */
	void onTextLine(TextNode node, int lineNumber, String lineString, LineIndent line);
}
