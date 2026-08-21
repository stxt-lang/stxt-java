package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;

/**
 * STXT-SPEC 9 and 11 (2026-08-21): the indentation of a comment line is validated exactly like a
 * node's (homogeneous, multiple of 4 spaces, level at most last node + 1), with the same error
 * codes. A comment still produces no node and never becomes the reference level.
 */
public class CommentIndentTest {

	private static ParseException error(String text) {
		return assertThrows(ParseException.class, () -> new Parser().parse(text));
	}

	@Test
	void mixedTabsAndSpacesInACommentIsMixedIndentation() {
		ParseException e = error("Root:\n\tFirst: 1\n\t    # mixed\n");
		assertEquals("INDENTATION_MIXED", e.getCode());
		assertEquals(3, e.getLine());
	}

	@Test
	void spacesNotMultipleOfFourInACommentIsInvalidNumberSpaces() {
		ParseException e = error("Root:\n  # two spaces\n    Child: 1\n");
		assertEquals("INDENTATION_SPACES_NOT_VALID", e.getCode());
		assertEquals(2, e.getLine());
	}

	@Test
	void aCommentDeeperThanLastNodePlusOneIsIndentationLevelNotValid() {
		ParseException e = error("Root:\n\t\t# two levels below Root\n\tChild: 1\n");
		assertEquals("INDENTATION_LEVEL_NOT_VALID", e.getCode());
		assertEquals(2, e.getLine());
	}

	@Test
	void commentsAtLevelZeroOneAndLastPlusOneAreValid() {
		List<Node> roots = new Parser().parse("# level 0\nRoot:\n\t# level 1, first child position\n\tFirst: 1\n\t\t# last + 1 after a childless node\n\tSecond: 2\n# level 0 again\nOther: x\n");

		assertEquals(2, roots.size());
		InlineNode root = (InlineNode) roots.get(0);
		assertEquals(List.of("First", "Second"), root.getChildren().stream().map(Node::getName).collect(Collectors.toList()));
		assertEquals("Other", roots.get(1).getName());
	}

	@Test
	void aNodeAfterALevelTwoCommentIsCheckedAgainstTheLastNodeNotTheComment() {
		// "\t\t# c" is valid (First + 1) but does not move the reference: Second at level 1 is a
		// sibling of First, and a level-3 node after the comment is still a jump from level 1.
		InlineNode root = (InlineNode) new Parser().parse("Root:\n\tFirst: 1\n\t\t# c\n\tSecond: 2\n").get(0);
		assertEquals(List.of("First", "Second"), root.getChildren().stream().map(Node::getName).collect(Collectors.toList()));
		assertEquals(0, ((InlineNode) root.getChildren().get(0)).getChildren().size());

		ParseException e = error("Root:\n\tFirst: 1\n\t\t# c\n\t\t\tDeep: 3\n");
		assertEquals("INDENTATION_LEVEL_NOT_VALID", e.getCode());
		assertEquals(4, e.getLine());
	}
}
