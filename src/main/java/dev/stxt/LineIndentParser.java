package dev.stxt;

import static dev.stxt.Constants.SPACE;
import static dev.stxt.Constants.TAB;
import static dev.stxt.Constants.TAB_SPACES;
import static dev.stxt.utils.StringUtils.rightTrim;

import dev.stxt.exceptions.ParseException;

class LineIndentParser {
	// -------------------------------------------------
	// parseLine
	// -------------------------------------------------

	public static LineIndent parseLine(String line, int numLine, ParseState parseState) {
		int stackSize = parseState.getStack().size();
		boolean lastNodeText = stackSize > 0 && parseState.getStack().peek().isTextNode();

		// Empty line
		if (line.trim().isEmpty()) {
			if (lastNodeText)
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
			if (lastNodeText && level >= stackSize) {
				return new LineIndent(level, rightTrim(line.substring(pointer)));
			}
		}

		// Comment
		if (isCommentLine(line))
			return null;

		// Strange indentation
		if (spaces > 0)
			throw new ParseException(numLine, "INVALID_NUMBER_SPACES", "There are " + spaces + " spaces before node");

		// 4) Caso general: devolver la línea sin la indentación consumida
		return new LineIndent(level, line.substring(pointer).trim());
	}

	private static boolean isCommentLine(String line) {
		return line.trim().startsWith("#");
	}
}
