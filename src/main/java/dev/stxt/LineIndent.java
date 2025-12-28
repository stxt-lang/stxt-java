package dev.stxt;

class LineIndent {
	// Indentation mode constants
	public final int indentLevel;
	public final String lineWithoutIndent;

	public LineIndent(int level, String line) {
		this.indentLevel = level;
		this.lineWithoutIndent = line;
	}
}
