package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;

class NodeNameValidationTest {

	@Test
	void acceptsDecomposedUnicodeNamesAfterNfcNormalization() {
		Node decomposed = new Parser().parse("Cafe\u0301: value\n").get(0);
		Node precomposed = new Parser().parse("Café: value\n").get(0);

		assertEquals("café", decomposed.getNormalizedName());
		assertEquals(precomposed.getNormalizedName(), decomposed.getNormalizedName());
	}
}
