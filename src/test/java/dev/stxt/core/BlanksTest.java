package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;

/** STXT-SPEC 4: a blank is only U+0020 or U+0009; any other Unicode space is content. */
class BlanksTest {

	private static final String NBSP = " ";

	private static String firstCode(String text) {
		return new Parser().parseResult(text).getErrors().get(0).getCode();
	}

	@Test
	void keepsAnNbspAsPartOfValuesAndBlockLines() {
		Node root = new Parser().parse("Root:\n\tTrailing: Joan" + NBSP + "\n\tLeading:" + NBSP + "Joan\n\tOnly:" + NBSP
				+ "\n\tBlock >>\n\t\tfirst" + NBSP + "\n\t\t" + NBSP + "\n\t\tin" + NBSP + "the" + NBSP + "middle\n").get(0);
		List<Node> children = ((InlineNode) root).getChildren();
		assertEquals("Joan" + NBSP, ((InlineNode) children.get(0)).getValue());
		assertEquals(NBSP + "Joan", ((InlineNode) children.get(1)).getValue());
		assertEquals(NBSP, ((InlineNode) children.get(2)).getValue());
		assertEquals(List.of("first" + NBSP, NBSP, "in" + NBSP + "the" + NBSP + "middle"), ((TextNode) children.get(3)).getTextLines());
	}

	@Test
	void aLineHoldingOnlyAnNbspIsNotEmpty() {
		assertEquals("INVALID_LINE", firstCode(NBSP + "\n"));
		assertEquals("BLOCK_VALUE_NOT_ALLOWED", firstCode("Block >>" + NBSP + "\n"));
		assertTrue(new Parser().parseResult("Root: x\n \t\n\n").getErrors().isEmpty());
		// String.trim() would also eat controls below U+0020; STXT does not
		assertEquals("INVALID_NODE_NAME", firstCode("Name: x\n"));
	}

	@Test
	void anNbspIsNotTrimmedFromANameWhichMakesItInvalid() {
		assertEquals("INVALID_NODE_NAME", firstCode("Name" + NBSP + ": x\n"));
		assertEquals("INVALID_NODE_NAME", firstCode("A" + NBSP + "B: x\n"));
		assertEquals("Name", new Parser().parse("Name \t: x\n").get(0).getName());
	}
}
