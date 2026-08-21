package dev.stxt;

import dev.stxt.utils.StringUtils;

/**
 * A source line already split into its indentation and its content, as produced by
 * {@link LineIndentParser#parseLine}. It is what tells the {@link Parser} whether the line opens
 * a node, continues a text block or is just a comment. Public and immutable fields, as in the
 * other ports.
 */
public final class LineIndent {
	/** Indentation level of the line (one level per tab or per {@link Constants#TAB_SPACES} spaces). */
	public final int indentLevel;
	/** Content of the line with the indentation already removed. */
	public final String lineWithoutIndent;
	/**
	 * True if the line is a comment ({@code #}). Its indentation has already been validated like
	 * a node's (spec 9), but it produces no node and never moves the hierarchy.
	 */
	public final boolean isComment;
	/** True if the line is a text line belonging to an open BLOCK node ({@code >>}). */
	public final boolean isBlock;
	/** Number of characters the indentation took up. */
	public final int indentLength;

	/**
	 * Creates a line already split into indentation and content.
	 *
	 * @param indentLevel indentation level of the line.
	 * @param lineWithoutIndent content of the line without its indentation.
	 * @param isComment true if the line is a comment.
	 * @param isBlock true if the line belongs to an open text block.
	 * @param indentLength number of characters the indentation took up.
	 */
	public LineIndent(int indentLevel, String lineWithoutIndent, boolean isComment, boolean isBlock, int indentLength) {
		this.indentLevel = indentLevel;
		this.lineWithoutIndent = lineWithoutIndent;
		this.isComment = isComment;
		this.isBlock = isBlock;
		this.indentLength = indentLength;
	}

	/** {@return true if the line has no content beyond blanks (space/tab only, spec 4)} */
	public boolean isEmpty() {
		return StringUtils.trim(lineWithoutIndent).isEmpty();
	}
}
