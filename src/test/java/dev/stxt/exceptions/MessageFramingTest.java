package dev.stxt.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;

/**
 * Message framing (since 0.10.0, the same in every port): {@code getMessage()} is only the
 * description, and the frame lives in {@code toString()}.
 */
class MessageFramingTest {

	@Test
	void parseExceptionMessageIsOnlyTheDescription() {
		ParseException ex = new ParseException(2, "INDENTATION_LEVEL_NOT_VALID", "Level of indent incorrect: 2");
		assertEquals("Level of indent incorrect: 2", ex.getMessage());
		assertEquals("INDENTATION_LEVEL_NOT_VALID", ex.getCode());
		assertEquals(2, ex.getLine());
		assertEquals("[INDENTATION_LEVEL_NOT_VALID] line 2: Level of indent incorrect: 2", ex.toString());
	}

	@Test
	void validationExceptionUsesTheSameFrame() {
		ValidationException ex = new ValidationException(5, "INVALID_VALUE", "Node 'x' Invalid Base64");
		assertEquals("Node 'x' Invalid Base64", ex.getMessage());
		assertEquals("[INVALID_VALUE] line 5: Node 'x' Invalid Base64", ex.toString());
	}

	@Test
	void baseExceptionFrameHasNoLine() {
		STXTException ex = new STXTException("SOME_CODE", "something happened");
		assertEquals("something happened", ex.getMessage());
		assertEquals("[SOME_CODE] something happened", ex.toString());

		SchemaException schema = new SchemaException("NODE_DUPLICATED", "Node 'a' already defined");
		assertEquals("Node 'a' already defined", schema.getMessage());
		assertEquals("[NODE_DUPLICATED] Node 'a' already defined", schema.toString());

		ResourceNotFoundException notFound = new ResourceNotFoundException("com.acme", "schema");
		assertEquals("Not found 'schema' in namespace: com.acme", notFound.getMessage());
		assertEquals("[RESOURCE_NOT_FOUND] Not found 'schema' in namespace: com.acme", notFound.toString());
	}

	@Test
	void theParserProducesTheSameContract() {
		ParseException ex = assertThrows(ParseException.class, () -> new Parser().parse("Root:\n\t\tChild: x\n"));
		assertEquals("INDENTATION_LEVEL_NOT_VALID", ex.getCode());
		assertEquals(2, ex.getLine());
		assertEquals("Level of indent incorrect: 2", ex.getMessage());
		assertEquals("[INDENTATION_LEVEL_NOT_VALID] line 2: Level of indent incorrect: 2", ex.toString());
	}
}
