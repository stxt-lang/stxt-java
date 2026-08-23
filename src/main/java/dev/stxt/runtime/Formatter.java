package dev.stxt.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.InlineNode;
import dev.stxt.LineIndent;
import dev.stxt.Node;
import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.processors.Observer;
import dev.stxt.runtime.NodeWriter.IndentStyle;
import dev.stxt.utils.StringUtils;

/**
 * Reformats an STXT document <b>line by line, over the original text</b>, so that nothing the
 * parse tree does not hold — comments, blank lines, the exact content of text blocks — is lost.
 * This is what distinguishes it from {@link NodeWriter}, which writes the canonical text form
 * of the tree and therefore drops comments and blank lines.
 *
 * <p>The rules are those of STXT-TREE-SPEC 12 ({@code stxt-impl/core/formatter.txt}), the same
 * for every tool of the ecosystem:
 * <ul>
 * <li>A line that <b>opens a node</b> is rendered in canonical form: the indentation of its
 * level in the requested style, the name as parsed, the namespace only where the source wrote it
 * (a child repeating its parent's namespace is redundant but legal, and dropping it would be an
 * edit, not a reformat), {@code : value} with exactly one space — or a bare {@code :} when there
 * is no value — or {@code  >>} for a block.</li>
 * <li>A <b>text line of a block</b> gets the indentation of the block (its level plus one) in
 * the requested style, followed by its content; any indentation the line had beyond the block's
 * is content (STXT-SPEC 10.2) and is kept exactly. A blank line of the block is {@code ""} in the
 * content (STXT-SPEC 10.3), so it is written with the indentation of the block too.</li>
 * <li>Every <b>other line</b> — a comment, a blank line outside a block, or a line the parse
 * tree does not describe because of a syntax error — is kept as the author wrote it, except that
 * its trailing blanks are removed and the whole indentation units at its start are converted one
 * for one to the requested style (a tab or four spaces in either style count as a unit; whatever
 * follows the last whole unit, a remainder included, is kept as it is).</li>
 * </ul>
 *
 * <p>The result is idempotent, round-trips between the two styles, and produces the same
 * canonical tree as the source; the line ending is kept (CRLF if the source holds any), a final
 * newline only where the source had one, and an initial BOM is removed. The document is parsed
 * without any schema: formatting has nothing to do with validation.
 */
public final class Formatter {

	private Formatter() {
	}

	/**
	 * Formats a document with tabs.
	 *
	 * @param text the document.
	 * @return the formatted text and the syntax errors found.
	 */
	public static FormatResult format(String text) {
		return format(text, IndentStyle.TABS);
	}

	/**
	 * Formats a document.
	 *
	 * @param text  the document.
	 * @param style indentation style to format with.
	 * @return the formatted text and the syntax errors found; see {@link FormatResult}.
	 */
	public static FormatResult format(String text, IndentStyle style) {
		// STXT-TREE-SPEC 12.1: an initial BOM is not kept
		if (text.startsWith("﻿"))
			text = text.substring(1);

		SourceLines sourceLines = new SourceLines();
		Parser parser = new Parser();
		parser.registerObserver(sourceLines);
		ParseResult result = parser.parseResult(text);

		String eol = text.contains("\r\n") ? "\r\n" : "\n";
		String[] lines = text.split("\r?\n", -1);
		List<String> out = new ArrayList<>(lines.length);
		for (int i = 0; i < lines.length; i++)
			out.add(formatLine(lines[i], i + 1, style, sourceLines));
		return new FormatResult(String.join(eol, out), result.getErrors());
	}

	private static String formatLine(String line, int lineNumber, IndentStyle style, SourceLines sourceLines) {
		Node node = sourceLines.nodeAt(lineNumber);
		if (node != null)
			return renderNode(node, line, style);
		SourceLines.TextLine text = sourceLines.textAt(lineNumber);
		if (text != null)
			return indent(text.node().getLevel() + 1, style) + text.line().lineWithoutIndent;
		return convertUnits(StringUtils.rightTrim(line), style);
	}

	/** The line that opens a node, in canonical form; the source line only tells whether it spelled the namespace out. */
	private static String renderNode(Node node, String line, IndentStyle style) {
		String head = node instanceof InlineNode ? line.substring(0, line.indexOf(':')) : line;
		String name = head.contains("(") ? node.getName() + " (" + node.getNamespace() + ")" : node.getName();
		String prefix = indent(node.getLevel(), style);
		if (node instanceof TextNode)
			return prefix + name + " >>";
		String value = ((InlineNode) node).getValue();
		return value.isEmpty() ? prefix + name + ":" : prefix + name + ": " + value;
	}

	/** Converts the whole indentation units at the start of the line and keeps the rest, remainder included. */
	private static String convertUnits(String line, IndentStyle style) {
		int consumed = 0;
		int units = 0;
		int unit = unitAt(line, consumed);
		while (unit > 0) {
			consumed += unit;
			units++;
			unit = unitAt(line, consumed);
		}
		return units == 0 ? line : indent(units, style) + line.substring(consumed);
	}

	/** Length of the whole indentation unit — a tab or four spaces — starting at position, or 0. */
	private static int unitAt(String line, int position) {
		if (line.startsWith("\t", position))
			return 1;
		return line.startsWith("    ", position) ? 4 : 0;
	}

	private static String indent(int level, IndentStyle style) {
		return (style == IndentStyle.SPACES_4 ? "    " : "\t").repeat(level);
	}

	/** The parse of a document seen as source lines: which line opened which node, and which line is text of which block. */
	private static final class SourceLines implements Observer {
		record TextLine(TextNode node, LineIndent line) {
		}

		private final Map<Integer, Node> nodeByLine = new HashMap<>();
		private final Map<Integer, TextLine> textByLine = new HashMap<>();

		@Override
		public void onCreate(Node node, String line) {
			nodeByLine.put(node.getLine(), node);
		}

		@Override
		public void onFinish(Node node) {
			// Formatting only needs to know where each node started
		}

		@Override
		public void onComment(int lineNumber, String line) {
			// Every line that opens no node is treated alike
		}

		@Override
		public void onTextLine(TextNode node, int lineNumber, String lineString, LineIndent line) {
			textByLine.put(lineNumber, new TextLine(node, line));
		}

		Node nodeAt(int lineNumber) {
			return nodeByLine.get(lineNumber);
		}

		TextLine textAt(int lineNumber) {
			return textByLine.get(lineNumber);
		}
	}
}
