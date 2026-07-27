package dev.stxt.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.schema.Schema;

public class TemplateParserTest {

	@Test
	void testValuesOnlyForEnum() {
		// STXT-TEMPLATE-SPEC 9/14.7: [values] sólo está permitido para el tipo ENUM
		String text = """
Template (@stxt.template): test.values.notenum
    Structure >>
        Foo: (1) TEXT [a, b]
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_ONLY_SUPPORTED_BY_ENUM", ex.getCode());
	}

	@Test
	void testEnumRequiresValues() {
		// STXT-TEMPLATE-SPEC 14.8: ENUM exige valores no vacíos
		String text = """
Template (@stxt.template): test.enum.empty
    Structure >>
        Foo: (1) ENUM
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_EMPTY_FOR_ENUM", ex.getCode());
	}

	@Test
	void testChildrenNotAllowedForTypeTemplate() {
		// STXT-TEMPLATE-SPEC 8.2/14.9: sólo INLINE y GROUP admiten hijos (equivalente al punto
		// 10, ya resuelto en el lado de schema)
		String text = """
Template (@stxt.template): test.leaf.children
    Structure >>
        Foo: (1) TEXT
            Bar: (1) TEXT
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("CHILDREN_NOT_ALLOWED_FOR_TYPE", ex.getCode());
	}

	@Test
	void testDescriptionAppliedToNode() {
		String text = """
Template (@stxt.template): test.desc.ok
    Structure >>
        Foo: (1) TEXT
    Description >>
        Foo: Descripcion de Foo
""";
		Node root = new Parser().parse(text).get(0);
		Schema sch = TemplateParser.transformNodeToSchema(root);
		assertEquals("Descripcion de Foo", sch.getNodeDefinition("Foo").getDescription());
	}

	@Test
	void testDescriptionMissingIsNull() {
		String text = """
Template (@stxt.template): test.desc.absent
    Structure >>
        Foo: (1) TEXT
""";
		Node root = new Parser().parse(text).get(0);
		Schema sch = TemplateParser.transformNodeToSchema(root);
		assertNull(sch.getNodeDefinition("Foo").getDescription());
	}

	@Test
	void testDescriptionUnknownNodeRejected() {
		String text = """
Template (@stxt.template): test.desc.notfound
    Structure >>
        Foo: (1) TEXT
    Description >>
        Bar: Descripcion
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("NODE_NOT_FOUND", ex.getCode());
	}

	@Test
	void testDescriptionDuplicatedRejected() {
		String text = """
Template (@stxt.template): test.desc.dup
    Structure >>
        Foo: (1) TEXT
    Description >>
        Foo: Uno
        Foo: Dos
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("DESCRIPTION_ALREADY_DEFINED", ex.getCode());
	}

	@Test
	void testDescriptionWithChildrenRejected() {
		String text = """
Template (@stxt.template): test.desc.children
    Structure >>
        Foo: (1) TEXT
    Description >>
        Foo:
            Bar: baz
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("CHILDREN_DESCRIPTION_NOT_ALLOWED", ex.getCode());
	}

	@Test
	void testDescriptionExternalNamespaceRejected() {
		String text = """
Template (@stxt.template): test.desc.external
    Structure >>
        Foo: (1) TEXT
    Description >>
        Foo (other.ns): Descripcion
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("EXTERNAL_DESCRIPTION_NOT_ALLOWED", ex.getCode());
	}

	@Test
	void testCrossNamespaceValuesRejected() {
		// STXT-TEMPLATE-SPEC 14.15: un nodo cross-namespace sólo puede fijar cardinalidad
		String text = """
Template (@stxt.template): test.cross.values
    Structure >>
        Foo (other.ns): (1) [a, b]
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_DEFINITION_NOT_ALLOWED", ex.getCode());
	}

	@Test
	void testCrossNamespaceChildrenRejected() {
		String text = """
Template (@stxt.template): test.cross.children
    Structure >>
        Foo (other.ns): (1)
            Bar: (1) TEXT
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("CHILDREN_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", ex.getCode());
	}

	@Test
	void testReferenceWithoutAtRejected() {
		// Regresión NPE: reaparición local sin tipo '@Nombre'
		String text = """
Template (@stxt.template): test.ref.noat
    Structure >>
        Foo: (1) TEXT
        Foo: (2)
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("NODE_DEFINED_MULTIPLE_TIMES", ex.getCode());
	}

	@Test
	void testReferenceWithChildrenRejected() {
		// STXT-TEMPLATE-SPEC 6.4: una referencia @Nombre no debe redefinir hijos
		String text = """
Template (@stxt.template): test.ref.children
    Structure >>
        Foo: (1) TEXT
        Foo: (2) @Foo
            Bar: (1) TEXT
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("CHILDREN_NOT_ALLOWED_IN_REFERENCE", ex.getCode());
	}
}
