package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.template.ChildLineParser;
import dev.stxt.template.TemplateParser;

/**
 * An empty ENUM value is an error (STXT-SCHEMA-SPEC 7.2 and 13, condition 14; STXT-TEMPLATE-SPEC
 * 14.14; since 0.10.0): {@code VALUE_EMPTY} at the line of the empty {@code Value} in a schema,
 * and at the line of the Structure line in a template. A whole empty list stays
 * {@code VALUES_REQUIRED}.
 */
public class EnumValueEmptyTest {

	private static ValidationException schemaError(String text) {
		Node root = new Parser().parse(text).get(0);
		return assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
	}

	private static ValidationException templateError(String text) {
		Node root = new Parser().parse(text).get(0);
		return assertThrows(ValidationException.class, () -> TemplateParser.transformNodeToSchema(root));
	}

	@Test
	void schemaEmptyValueIsValueEmptyAtTheLineOfTheValue() {
		String schema = """
				Schema (@stxt.schema): com.acme.book
				\tNode: Book
				\t\tType: ENUM
				\t\tValues:
				\t\t\tValue: x
				\t\t\tValue:
				\t\t\tValue: y
				""";
		ValidationException ex = schemaError(schema);
		assertEquals("VALUE_EMPTY", ex.getCode());
		assertEquals(6, ex.getLine());
		assertEquals("Value of ENUM cannot be empty", ex.getMessage());
	}

	@Test
	void schemaOnlyEmptyValueIsValueEmptyNotValuesRequired() {
		String schema = """
				Schema (@stxt.schema): com.acme.book
				\tNode: Book
				\t\tType: ENUM
				\t\tValues:
				\t\t\tValue:
				""";
		ValidationException ex = schemaError(schema);
		assertEquals("VALUE_EMPTY", ex.getCode());
		assertEquals(5, ex.getLine());
	}

	@Test
	void schemaNonEmptyValuesStillLoad() {
		String schema = """
				Schema (@stxt.schema): com.acme.book
				\tNode: Book
				\t\tType: ENUM
				\t\tValues:
				\t\t\tValue: x
				\t\t\tValue: y
				""";
		Node root = new Parser().parse(schema).get(0);
		assertDoesNotThrow(() -> SchemaParser.transformNodeToSchema(root));
	}

	@Test
	void templateEmptyItemInTheMiddleIsValueEmpty() {
		ValidationException ex = assertThrows(ValidationException.class, () -> ChildLineParser.parse("(1) ENUM [a, , b]", 7));
		assertEquals("VALUE_EMPTY", ex.getCode());
		assertEquals(7, ex.getLine());
	}

	@Test
	void templateTrailingCommaIsValueEmpty() {
		ValidationException ex = assertThrows(ValidationException.class, () -> ChildLineParser.parse("(1) ENUM [a, b,]", 7));
		assertEquals("VALUE_EMPTY", ex.getCode());
		assertEquals(7, ex.getLine());
	}

	@Test
	void templateWholeEmptyListStaysValuesRequired() {
		String template = """
				Template (@stxt.template): com.acme.book
				\tStructure >>
				\t\tBook:
				\t\t\tField: (1) ENUM []
				""";
		ValidationException ex = templateError(template);
		assertEquals("VALUES_REQUIRED", ex.getCode());
	}

	@Test
	void templateEmptyItemIsReportedAtTheStructureLine() {
		String template = """
				Template (@stxt.template): com.acme.book
				\tStructure >>
				\t\tBook:
				\t\t\tField: (1) ENUM [a, , b]
				""";
		ValidationException ex = templateError(template);
		assertEquals("VALUE_EMPTY", ex.getCode());
		assertEquals(4, ex.getLine());
	}

	@Test
	void templateNonEmptyListStillLoads() {
		assertDoesNotThrow(() -> ChildLineParser.parse("(1) ENUM [a, b]", 1));
		assertEquals(2, ChildLineParser.parse("(1) ENUM [ a , b ]", 1).getValues().length);
	}
}
