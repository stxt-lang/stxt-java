package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.stxt.LineIndent;
import dev.stxt.LineIndentParser;
import dev.stxt.exceptions.ParseException;

/** {@link LineIndentParser#parseLine}: the public line splitter, same contract as the other ports. */
class LineIndentTest {

	@Test
	void splitsIndentationAndContent() {
		LineIndent li = LineIndentParser.parseLine("\t\tName: value", false, 1, 1);
		assertEquals(2, li.indentLevel);
		assertEquals("Name: value", li.lineWithoutIndent);
		assertFalse(li.isComment);
		assertFalse(li.isBlock);
		assertEquals(2, li.contentStart);
		assertFalse(li.isEmpty());
	}

	@Test
	void aCommentKeepsTheTextAfterTheHashVerbatim() {
		LineIndent comment = LineIndentParser.parseLine("\t# hi", false, 0, 1);
		assertTrue(comment.isComment);
		assertEquals(" hi", comment.lineWithoutIndent);
		assertEquals(1, comment.indentLevel);
	}

	@Test
	void aLineDeeperThanAnOpenBlockIsText() {
		LineIndent text = LineIndentParser.parseLine("\t\t  # not a comment ", true, 1, 3);
		assertTrue(text.isBlock);
		assertFalse(text.isComment);
		assertEquals("  # not a comment", text.lineWithoutIndent);
		assertEquals(2, text.contentStart);
	}

	@Test
	void emptyLinesAreNeverAnErrorAndAreTextInsideABlock() {
		LineIndent outside = LineIndentParser.parseLine("   ", false, 0, 1);
		assertTrue(outside.isEmpty());
		assertFalse(outside.isBlock);

		LineIndent inside = LineIndentParser.parseLine("", true, 2, 1);
		assertTrue(inside.isBlock);
		assertEquals("", inside.lineWithoutIndent);
	}

	@Test
	void indentationErrorsKeepTheirCodes() {
		assertEquals("INDENTATION_MIXED", assertThrows(ParseException.class,
			() -> LineIndentParser.parseLine("\t  A: x", false, 0, 1)).getCode());
		assertEquals("INDENTATION_SPACES_NOT_VALID", assertThrows(ParseException.class,
			() -> LineIndentParser.parseLine("  A: x", false, 0, 1)).getCode());
		assertEquals("INDENTATION_LEVEL_NOT_VALID", assertThrows(ParseException.class,
			() -> LineIndentParser.parseLine("\t\tA: x", false, 0, 1)).getCode());
	}
}
