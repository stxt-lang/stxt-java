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
import test.FileTestLoction;
import test.JSON;
import test.FileChecks;

public class SchemaParserTest {
	@Test
	void testReadSchema() throws IOException {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
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
		// STXT-SCHEMA-SPEC 9.1/13.5: sólo INLINE y GROUP admiten hijos
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
		// STXT-SCHEMA-SPEC 13.9: 'Value' duplicado tras la normalización por trim. La vía de
		// template ya lo rechazaba en ChildLineParser; la de schema se lo tragaba en silencio
		// porque los valores se guardan en un Set.
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
		// La regla es "tras la normalización por trim": 'alta' y '  alta  ' son el mismo valor
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
		// STXT-SCHEMA-SPEC 10/13.7: Min no debe ser mayor que Max
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
	void testMetaSchemaRejectsUnknownTypeAtLoad() {
		// STXT-SCHEMA-SPEC 13.4/15.2: un Type desconocido debe fallar al cargar el schema,
		// no sólo al validar documentos contra él
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
		// STXT-SCHEMA-SPEC 15.2: 'Values' es de tipo GROUP, no admite valor inline
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
		assertEquals("INVALID_VALUE", ex.getCode());
	}

	private void showSchema(Schema sch) {
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}