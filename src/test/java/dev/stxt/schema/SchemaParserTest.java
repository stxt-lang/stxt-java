package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLocation;
import test.JSON;
import test.FileChecks;

public class SchemaParserTest {
	@Test
	void testReadSchema() throws IOException {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLocation.getFile(""));
		String schema = resourcesLoader.retrieve("@stxt.schema", "com.example.docs");
		System.out.println("exists: " + schema);

		Parser parser = new Parser();
		List<Node> nodes = parser.parse(schema);
		for (Node node : nodes) {
			System.out.println(JSON.toJson(node));
			Schema sch = SchemaParser.transformNodeToSchema(node);
			showSchema(sch);
			FileChecks.checkContentWithJsonFile(sch, "schema_json", "com.example.docs");
		}
	}

	@Test
	void testChildrenNotAllowedForType() {
		// STXT-SCHEMA-SPEC 9.1/13.5: only INLINE and GROUP accept children
		String text = """
Schema (@stxt.schema): test.leaf.children
    Node: Foo
        Type: TEXT
        Children:
            Child: Bar
    Node: Bar
""";
		Node root = new Parser().parse(text).get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("CHILDREN_NOT_ALLOWED_FOR_TYPE", ex.getCode());
	}

	@Test
	void testDuplicatedValueRejected() {
		// STXT-SCHEMA-SPEC 13.9: duplicated 'Value' after trim normalization. The template route
		// already rejected it in ChildLineParser; the schema one swallowed it silently because
		// the values are kept in a Set.
		String text = """
Schema (@stxt.schema): test.value.dup
    Node: Estado
        Type: ENUM
        Values:
            Value: alta
            Value: alta
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("VALUE_DUPLICATED", ex.getCode());
	}

	@Test
	void testDuplicatedValueAfterTrimRejected() {
		// The rule is "after trim normalization": 'alta' and '  alta  ' are the same value
		String text = """
Schema (@stxt.schema): test.value.dup.trim
    Node: Estado
        Type: ENUM
        Values:
            Value: alta
            Value:   alta
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("VALUE_DUPLICATED", ex.getCode());
	}

	@Test
	void testMinGreaterThanMax() {
		// STXT-SCHEMA-SPEC 10/13.7: Min must not be greater than Max
		String text = """
Schema (@stxt.schema): test.min.max
    Node: Foo
        Children:
            Child: Bar
                Min: 3
                Max: 1
    Node: Bar
""";
		Node root = new Parser().parse(text).get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("MIN_GREATER_THAN_MAX", ex.getCode());
	}

	@Test
	void testCardinalityBound() {
		// STXT-SCHEMA-SPEC 10: Min/Max are bounded to 2^32 - 1; the bound itself is legal
		String ok = """
Schema (@stxt.schema): test.card.bound
    Node: Foo
        Children:
            Child: Bar
                Max: 4294967295
    Node: Bar
""";
		SchemaParser.transformNodeToSchema(new Parser().parse(ok).get(0));

		// one over the bound, and a literal too big even for a long, are CARDINALITY_NOT_VALID
		for (String max : new String[] { "4294967296", "99999999999999999999999999" }) {
			String bad = ok.replace("4294967295", max);
			Node root = new Parser().parse(bad).get(0);
			ParseException ex = assertThrows(ParseException.class, () -> SchemaParser.transformNodeToSchema(root));
			assertEquals("CARDINALITY_NOT_VALID", ex.getCode());
		}
	}

	@Test
	void testMetaSchemaRejectsUnknownTypeAtLoad() {
		// STXT-SCHEMA-SPEC 13.4/15.2: an unknown Type must fail when the schema is loaded,
		// not only when validating documents against it
		String text = """
Schema (@stxt.schema): test.unknown.type
    Node: Foo
        Type: NOPE
""";
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(new SchemaProviderMeta()));
		ValidationException ex = assertThrows(ValidationException.class, () -> parser.parse(text));
		assertEquals("INVALID_VALUE", ex.getCode());
	}

	@Test
	void testMetaSchemaRejectsValueOnValuesNode() {
		// STXT-SCHEMA-SPEC 15.2: 'Values' is of type GROUP, it accepts no inline value
		String text = """
Schema (@stxt.schema): test.values.group
    Node: Foo
        Type: ENUM
        Values: texto
            Value: a
""";
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(new SchemaProviderMeta()));
		ValidationException ex = assertThrows(ValidationException.class, () -> parser.parse(text));
		assertEquals("VALUE_NOT_ALLOWED", ex.getCode());
	}

	@Test
	void testSchemaNodeNameMustBeAValidStxtNodeName() {
		String text = """
Schema (@stxt.schema): test.invalid.node
    Node: Invalid!
""";
		Node root = new Parser().parse(text).get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("INVALID_NODE_NAME", ex.getCode());
	}

	@Test
	void testSchemaChildNameMustBeAValidStxtNodeName() {
		String text = """
Schema (@stxt.schema): test.invalid.child
    Node: Root
        Children:
            Child: Invalid!
""";
		Node root = new Parser().parse(text).get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("INVALID_NODE_NAME", ex.getCode());
	}

	private void showSchema(Schema sch) {
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}
