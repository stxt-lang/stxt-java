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
    
	public static LineIndent parseLine(String line, boolean lastNodeBlock, int lastLevel, int numLine) {
        // Walk the line
        int level = 0;
        int spaces = 0;
        int pointer = 0;
        boolean sawSpace = false;
        boolean sawTab = false;

        while (pointer < line.length()) {
            char c = line.charAt(pointer);

            if (c == SPACE) {
                sawSpace = true;
                spaces++;
                if (spaces == TAB_SPACES) {
                    level++;
                    spaces = 0;
                }
            } else if (c == TAB) {
                sawTab = true;
                level++;
                spaces = 0;
            } else if (c == COMMENT_CHAR) {
                return null;
            } else {
                // First character that is not space/tab/comment => end of indentation
                break;
            }

            pointer++;

            // Inside the text block
            if (lastNodeBlock && level > lastLevel) {
                String text = rightTrim(line.substring(pointer));
                // The prefix covering the block level must be homogeneous (spec 10.2, rule 2);
                // empty lines are always preserved and are exempt (spec 10.3)
                if (sawSpace && sawTab && !text.isEmpty())
                    throw new ParseException(numLine, "MIXED_INDENTATION", "Mixed tabs and spaces in indentation");
                return new LineIndent(level, text);
            }
        }

        // At this point we are already outside the text block (if there was one)

        // Empty
        if (pointer == line.length()) {
            if (lastNodeBlock)  return new LineIndent(lastLevel + 1, "");
            else                return null;
        }

        // Spaces and tabs mixed in the indentation (spec 8.1 and 8.3)
        if (sawSpace && sawTab)
            throw new ParseException(numLine, "MIXED_INDENTATION", "Mixed tabs and spaces in indentation");

        // Space indentation is not a multiple of 4
        if (spaces > 0)
            throw new ParseException(numLine, "INVALID_NUMBER_SPACES", "There are " + spaces + " spaces before node");
        
        // Validate the level
        if (level > (lastLevel + 1))
            throw new ParseException(numLine, "INDENTATION_LEVEL_NOT_VALID",
                    "Level of indent incorrect: " + level);            

        // 4) General case: return the line without the indentation already consumed
        return new LineIndent(level, line.substring(pointer).trim());
	}
}
