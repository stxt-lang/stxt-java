package dev.stxt;

import static dev.stxt.Constants.SPACE;
import static dev.stxt.Constants.TAB;
import static dev.stxt.Constants.TAB_SPACES;
import static dev.stxt.utils.StringUtils.rightTrim;

class LineIndent {
	// Indentation mode constants
	public final int indentLevel;
	public final String lineWithoutIndent;

	private LineIndent(int level, String line) {
		this.indentLevel = level;
		this.lineWithoutIndent = line;
	}

	// -------------------------------------------------
	// parseLine
	// -------------------------------------------------

	public static LineIndent parseLine(String line, int numLine, ParseState parseState) throws ParseException {
		int stackSize = parseState.getStack().size();
		boolean lastNodeMultiline = stackSize > 0 && parseState.getStack().peek().isMultiline();

		// Empty line
		if (line.trim().isEmpty()) {
			if (lastNodeMultiline)
				return new LineIndent(stackSize, "");
			else
				return null;
		}

		// Recorremos
		int level = 0;
		int spaces = 0;
		int pointer = 0;

		while (pointer < line.length()) {
			char c = line.charAt(pointer);

			if (c == SPACE) {
				spaces++;
				if (spaces == TAB_SPACES) {
					level++;
					spaces = 0;
				}
			} else if (c == TAB) {
				level++;
				spaces = 0;
			} else {
				// Primer carácter no espacio/tab → fin de indentación
				break;
			}

			pointer++;

			// Dentro de multilínea: no consumir más niveles de los del nodo
			if (lastNodeMultiline && level >= stackSize) {
				return new LineIndent(level, rightTrim(line.substring(pointer)));
			}
		}

		// Comment
		if (isCommentLine(line))
			return null;

		// Strange identation
		if (spaces > 0)
			throw new ParseException(numLine, "INVALID_NUMBER_SPACES", "There are " + spaces + " spaces before node");

		// 4) Caso general: devolver la línea sin la indentación consumida
		return new LineIndent(level, line.substring(pointer).trim());
	}

	private static boolean isCommentLine(String line) {
		return line.trim().startsWith("#");
	}
}
