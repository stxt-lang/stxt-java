package dev.stxt;

import static dev.stxt.Constants.SPACE;
import static dev.stxt.Constants.TAB;
import static dev.stxt.Constants.TAB_SPACES;
import static dev.stxt.Constants.COMMENT_CHAR;
import static dev.stxt.utils.StringUtils.rightTrim;

import dev.stxt.exceptions.ParseException;

class LineIndentParser {

    private LineIndentParser() {
    }
    
	public static LineIndent parseLine(String line, int numLine, ParseState parseState) {
		int stackSize = parseState.getStack().size();
		boolean lastNodeText = stackSize > 0 && parseState.getStack().peek().isTextNode();

		if (lastNodeText) return parseLineAfterBlock(stackSize, line, numLine);
		else              return parseLineAfterInline(stackSize, line, numLine);
	}

	private static LineIndent parseLineAfterBlock(int stackSize, String line, int numLine)
    {
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
            } else if (c == COMMENT_CHAR) {
                return null;
            } else {
                // Primer carácter no espacio/tab => fin de indentación
                break;
            }

            pointer++;

            // Dentro de multilínea: no consumir más niveles de los del nodo
            if (level >= stackSize) 
                return new LineIndent(level, rightTrim(line.substring(pointer)));
        }
        
        // Empty
        if (pointer == line.length()) return new LineIndent(stackSize, "");

        // Strange indentation
        if (spaces > 0)
            throw new ParseException(numLine, "INVALID_NUMBER_SPACES", "There are " + spaces + " spaces before node");

        // 4) Caso general: devolver la línea sin la indentación consumida
        return new LineIndent(level, line.substring(pointer).trim());
    }

    private static LineIndent parseLineAfterInline(int stackSize, String line, int numLine)
    {
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
            } else if (c == COMMENT_CHAR) {
                return null;
            } else {
                // Primer carácter no espacio/tab → fin de indentación
                break;
            }

            pointer++;
        }

        // Empty
        if (pointer == line.length()) return null;

        // Strange indentation
        if (spaces > 0)
            throw new ParseException(numLine, "INVALID_NUMBER_SPACES", "There are " + spaces + " spaces before node");

        // 4) Caso general: devolver la línea sin la indentación consumida
        return new LineIndent(level, line.substring(pointer).trim());
    }
}
