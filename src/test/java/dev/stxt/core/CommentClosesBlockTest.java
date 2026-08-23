package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.exceptions.ParseException;

/**
 * STXT-SPEC 6.1 and 9.1: a comment at the level of the block node (or shallower) closes the
 * block. A block is a literal and cannot be commented from inside.
 */
public class CommentClosesBlockTest {

	private static List<String> codes(String text) {
		return new Parser().parseResult(text).getErrors().stream().map(ParseException::getCode).collect(Collectors.toList());
	}

	@Test
	void aCommentAtTheLevelOfTheBlockNodeClosesTheBlock() {
		InlineNode root = (InlineNode) new Parser().parse("Root:\n\tBody >>\n\t\tfirst\n\t\t# still text\n\t# closes the block\n\tAfter: sibling\n").get(0);
		List<Node> children = root.getChildren();

		assertEquals(2, children.size());
		TextNode body = assertInstanceOf(TextNode.class, children.get(0));
		assertEquals(List.of("first", "# still text"), body.getTextLines());
		assertEquals("After", children.get(1).getName());
		assertEquals("sibling", ((InlineNode) children.get(1)).getValue());
	}

	@Test
	void aShallowerCommentClosesOnlyTheBlockNotItsAncestors() {
		InlineNode root = (InlineNode) new Parser().parse("Root:\n\tBody >>\n\t\tline\n# root-level comment\n\tAfter: x\n").get(0);

		assertEquals(List.of("Body", "After"), root.getChildren().stream().map(Node::getName).collect(Collectors.toList()));
		assertEquals(List.of("line"), ((TextNode) root.getChildren().get(0)).getTextLines());
	}

	@Test
	void textAfterAClosingCommentIsAParseErrorInsteadOfALostLine() {
		assertEquals(List.of("INDENTATION_LEVEL_NOT_VALID"), codes("Root:\n\tBody >>\n\t\tfirst\n\t# oops\n\t\tsecond\n"));
		assertEquals(List.of("INDENTATION_LEVEL_NOT_VALID"), codes("Body >>\n\tfirst\n# oops\n\tsecond\n"));
	}

	@Test
	void aCommentAfterTheLastLineOfABlockIsStillJustAComment() {
		InlineNode root = (InlineNode) new Parser().parse("Root:\n\tBody >>\n\t\tonly\n\t# trailing comment\n").get(0);

		assertEquals(List.of("only"), ((TextNode) root.getChildren().get(0)).getTextLines());
	}
}
