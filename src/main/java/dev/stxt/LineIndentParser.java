package dev.stxt;

import static dev.stxt.Constants.SPACE;
import static dev.stxt.Constants.TAB;
import static dev.stxt.Constants.TAB_SPACES;
import static dev.stxt.Constants.COMMENT_CHAR;
import dev.stxt.utils.StringUtils;
import static dev.stxt.utils.StringUtils.rightTrim;

import dev.stxt.exceptions.ParseException;

/**
 * Splits a source line into its indentation and its content, and classifies it as a comment, a
 * text line of an open block or a regular line (STXT-SPEC 8, 9, 10.2, 10.3 and 11).
 */
public final class LineIndentParser {

    private LineIndentParser() {
    }

	/**
	 * Splits a source line into its indentation and its content.
	 *
	 * Indentation is one level per tab or per {@link Constants#TAB_SPACES} spaces; mixing both,
	 * using a number of spaces that is not a multiple of four or going more than one level deeper
	 * than the previous node are errors (spec 8.1 and 8.3). Comment lines are validated exactly
	 * like node lines (spec 9 and 11): they produce no node and never move the hierarchy. Only
	 * empty lines are exempt.
	 *
	 * @param line source line, with its indentation.
	 * @param lastNodeBlock true if the node currently open is a BLOCK text node.
	 * @param lastLevel indentation level of the node currently open.
	 * @param numLine line number, for the error messages.
	 * @return the line already split into indentation and content, never {@code null}.
	 * @throws ParseException with code {@code INDENTATION_MIXED}, {@code INDENTATION_SPACES_NOT_VALID}
	 *         or {@code INDENTATION_LEVEL_NOT_VALID} if the indentation is not valid.
	 */
	public static LineIndent parseLine(String line, boolean lastNodeBlock, int lastLevel, int numLine) {
        // Walk the line
        int level = 0;
        int spaces = 0;
        int pointer = 0;
        boolean sawSpace = false;
        boolean sawTab = false;
        boolean isComment = false;

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
                // Comment: produces no node, but its indentation is validated below exactly like a
                // node's (spec 9, 11). Reached only when the line is not block text (a '#' deeper
                // than an open block is caught as text below, before getting here), so with an open
                // block a comment always has indent <= the block node and the Parser closes it
                // (spec 9.1) and hands the comment over to the observers.
                isComment = true;
                break;
            } else {
                // First character that is not space/tab/comment => end of indentation
                break;
            }

            // Inside the text block
            if (lastNodeBlock && level > lastLevel) {
                String text = rightTrim(line.substring(pointer + 1));
                // The prefix covering the block level must be homogeneous (spec 10.2, rule 2);
                // empty lines are always preserved and are exempt (spec 10.3)
                if (sawSpace && sawTab && !text.isEmpty())
                    throw new ParseException(numLine, "INDENTATION_MIXED", "Mixed tabs and spaces in indentation");
                // pointer is the index of the indentation character that crossed the block
                // level; the indentation took pointer + 1 characters
                return new LineIndent(level, text, false, true, pointer + 1);
            }

            pointer++;
        }

        // At this point we are already outside the text block (if there was one)

        // Empty
        if (pointer == line.length()) {
            if (lastNodeBlock)  return new LineIndent(level, "", false, true, pointer);
            else                return new LineIndent(level, "", false, false, pointer);
        }

        // Spaces and tabs mixed in the indentation (spec 8.1 and 8.3)
        if (sawSpace && sawTab)
            throw new ParseException(numLine, "INDENTATION_MIXED", "Mixed tabs and spaces in indentation");

        // Space indentation is not a multiple of 4
        if (spaces > 0)
            throw new ParseException(numLine, "INDENTATION_SPACES_NOT_VALID", "There are " + spaces + " spaces before node");
        
        // Validate the level progression (no jumps, spec 11.3). Comments included (spec 9):
        // lastLevel is the level of the last NODE, a comment never becomes the reference.
        if (level > (lastLevel + 1))
            throw new ParseException(numLine, "INDENTATION_LEVEL_NOT_VALID",
                    "Level of indent incorrect: " + level);

        // Comment: the text after '#', verbatim
        if (isComment)
            return new LineIndent(level, line.substring(pointer + 1), true, false, pointer);

        // General case: return the line without the indentation already consumed
        // Blank-only trim (spec 4): an NBSP after the value is part of it
        return new LineIndent(level, StringUtils.trim(line.substring(pointer)), false, false, pointer);
	}
}
