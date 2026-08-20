package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;

class NodeNameValidationTest {

	@Test
	void acceptsCombiningMarksAndStillRequiresALetterOrDigit() {
		// STXT-SPEC 4.2: Mn and Mc are name characters; Me is not; a name of only marks is not a name
		java.util.List<Node> nodes = new Parser().parse("\u0939\u093f\u0902\u0926\u0940: x\nQ\u0301: y\n");
		assertEquals("\u0939\u093f\u0902\u0926\u0940", nodes.get(0).getCanonicalName());
		assertEquals("q\u0301", nodes.get(1).getCanonicalName());

		assertEquals("INVALID_NODE_NAME", new Parser().parseResult("\u0301: only a mark\n").getErrors().get(0).getCode());
		assertEquals("INVALID_NODE_NAME", new Parser().parseResult("a\u20dd: enclosing mark\n").getErrors().get(0).getCode());
	}

	@Test
	void acceptsDecomposedUnicodeNamesAfterNfcNormalization() {
		Node decomposed = new Parser().parse("Cafe\u0301: value\n").get(0);
		Node precomposed = new Parser().parse("Café: value\n").get(0);

		assertEquals("café", decomposed.getNormalizedName());
		assertEquals(precomposed.getNormalizedName(), decomposed.getNormalizedName());
	}
}
