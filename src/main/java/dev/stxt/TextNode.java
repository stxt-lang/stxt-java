package dev.stxt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * BLOCK text node of the STXT tree ({@code Name >>}): an ordered list of literal text lines. It
 * has no inline value and no children; its content is only text.
 *
 * <p>Overloads with two strings always take the second one as the <em>content</em> (the text);
 * the namespace only exists in the three-argument forms.
 */
public final class TextNode extends Node {
	private final List<String> lines = new ArrayList<>();

	/**
	 * Creates an empty text node with no declared namespace and no known source line.
	 *
	 * @param name name of the node.
	 */
	public TextNode(String name) {
		this(name, null, (List<String>) null, NO_LINE);
	}

	/**
	 * Creates a text node with its text, no declared namespace and no known source line.
	 *
	 * @param name name of the node.
	 * @param text text of the node; it is split into lines at every line break.
	 */
	public TextNode(String name, String text) {
		this(name, null, text);
	}

	/**
	 * Creates a text node with a declared namespace and its text, and no known source line.
	 *
	 * @param name name of the node.
	 * @param namespace namespace the node declares, or {@code null}/empty for none.
	 * @param text text of the node; it is split into lines at every line break.
	 */
	public TextNode(String name, String namespace, String text) {
		this(name, namespace, text == null ? null : Arrays.asList(splitLines(text)), NO_LINE);
	}

	/**
	 * Creates a text node with a declared namespace and its lines, and no known source line.
	 *
	 * @param name name of the node.
	 * @param namespace namespace the node declares, or {@code null}/empty for none.
	 * @param lines text lines of the node, or {@code null} for none.
	 */
	public TextNode(String name, String namespace, List<String> lines) {
		this(name, namespace, lines, NO_LINE);
	}

	/**
	 * Creates a text node with its full description. This is the constructor the {@link Parser}
	 * uses (with no lines yet: it appends them with {@link #addTextLine(String)}).
	 *
	 * @param name name of the node.
	 * @param namespace namespace the node declares, or {@code null}/empty for none.
	 * @param lines text lines of the node, or {@code null} for none.
	 * @param line source line, or {@link #NO_LINE}.
	 * @throws dev.stxt.exceptions.ParseException if the name or the namespace are not valid.
	 */
	public TextNode(String name, String namespace, List<String> lines, int line) {
		super(name, namespace, line);
		if (lines != null)
			this.lines.addAll(lines);
	}

	// ----------------------------------------------------------------
	// Text
	// ----------------------------------------------------------------

	/** {@return the text lines of the node, in order, as a read-only view} */
	public List<String> getTextLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * Replaces the whole text of the node.
	 *
	 * @param text new text; it is split into lines at every line break. {@code null} empties the node.
	 */
	public void setText(String text) {
		lines.clear();
		if (text != null)
			lines.addAll(Arrays.asList(splitLines(text)));
	}

	/**
	 * Replaces the whole text of the node with the given lines.
	 *
	 * @param newLines new text lines; {@code null} empties the node.
	 */
	public void setTextLines(List<String> newLines) {
		lines.clear();
		if (newLines != null)
			lines.addAll(newLines);
	}

	/**
	 * Appends a text line.
	 *
	 * @param line text line to append.
	 */
	public void addTextLine(String line) {
		lines.add(line);
	}

	/** Removes every text line. */
	public void clearText() {
		lines.clear();
	}

	/**
	 * Removes the final empty lines ({@code ""} elements at the end of the lines). The
	 * {@link Parser} calls it when the block closes (STXT-SPEC §10.3: the final empty lines of a
	 * block are not content); it is public because a programmatically built node may want the
	 * same normalization before writing.
	 */
	public void removeTrailingEmptyLines() {
		while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty())
			lines.remove(lines.size() - 1);
	}

	@Override
	public String getText() {
		return String.join("\n", lines);
	}

	@Override
	public boolean isTextNode() {
		return true;
	}

	// LF or CRLF; the trailing part after the last break is a line too (possibly empty)
	static String[] splitLines(String text) {
		return text.split("\\r?\\n", -1);
	}

	@Override
	void describe(StringBuilder sb) {
		sb.append(", lines=").append(lines.size());
	}
}
