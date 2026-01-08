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
		Node lastNode = parseState.getStack().peek();
		boolean lastNodeBlock = lastNode != null && lastNode.isTextNode();
		
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
                // Primer carácter no espacio/tab/comentario => fin de indentación
                break;
            }

            pointer++;

            // Dentro del bloque de texto
            if (lastNodeBlock && level > lastNode.getLevel()) 
                return new LineIndent(level, rightTrim(line.substring(pointer)));
        }        
        
        // En este punto ya estamos fuera de bloque de texto (si existía)
        
        // Empty
        if (pointer == line.length()) {
            if (lastNodeBlock)  return new LineIndent(lastNode.getLevel()+1, "");
            else                return null;
        }

        // Indentación no és múltiplo de 4 con espacios
        if (spaces > 0)
            throw new ParseException(numLine, "INVALID_NUMBER_SPACES", "There are " + spaces + " spaces before node");

        // 4) Caso general: devolver la línea sin la indentación consumida
        return new LineIndent(level, line.substring(pointer).trim());
	}
}
