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
		// STXT-TEMPLATE-SPEC 9/14.7: [values] is only allowed for the ENUM type
		String text = """
Template (@stxt.template): test.values.notenum
    Structure >>
        Foo: (1) TEXT [a, b]
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_NOT_ALLOWED_FOR_TYPE", ex.getCode());
	}

	@Test
	void testEnumRequiresValues() {
		// STXT-TEMPLATE-SPEC 14.8: ENUM requires non-empty values
		String text = """
Template (@stxt.template): test.enum.empty
    Structure >>
        Foo: (1) ENUM
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_REQUIRED", ex.getCode());
	}

	@Test
	void testChildrenNotAllowedForTypeTemplate() {
		// STXT-TEMPLATE-SPEC 8.2/14.9: only INLINE and GROUP accept children (the counterpart of
		// point 10, already settled on the schema side)
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
		assertEquals("DESCRIPTION_NODE_NOT_FOUND", ex.getCode());
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
		assertEquals("DESCRIPTION_DUPLICATED", ex.getCode());
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
		assertEquals("DESCRIPTION_CHILDREN_NOT_ALLOWED", ex.getCode());
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
		assertEquals("DESCRIPTION_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", ex.getCode());
	}

	@Test
	void testCrossNamespaceValuesRejected() {
		// STXT-TEMPLATE-SPEC 14.15: a cross-namespace node may only set cardinality
		String text = """
Template (@stxt.template): test.cross.values
    Structure >>
        Foo (other.ns): (1) [a, b]
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_NOT_ALLOWED_IN_EXTERNAL_NAMESPACE", ex.getCode());
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
		// NPE regression: local reappearance without a '@Name' type
		String text = """
Template (@stxt.template): test.ref.noat
    Structure >>
        Foo: (1) TEXT
        Foo: (2)
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("REFERENCE_REQUIRED", ex.getCode());
	}

	@Test
	void testReferenceWithValuesRejected() {
		// STXT-TEMPLATE-SPEC 6.4: a @Name reference must not redefine ENUM values.
		// A different code from the cross-namespace one, which used to share VALUES_DEFINITION_NOT_ALLOWED.
		String text = """
Template (@stxt.template): test.ref.values
    Structure >>
        Foo: (1) ENUM [a, b]
        Foo: (2) @Foo [c, d]
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUES_NOT_ALLOWED_IN_REFERENCE", ex.getCode());
	}

	@Test
	void testReferenceWithChildrenRejected() {
		// STXT-TEMPLATE-SPEC 6.4: a @Name reference must not redefine children
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

	@Test
	void testDuplicatedValueRejected() {
		// STXT-TEMPLATE-SPEC 14.14: the same code as through the schema route (VALUE_DUPLICATED),
		// so the condition does not change name depending on the door it came in through
		String text = """
Template (@stxt.template): test.value.dup
    Structure >>
        Estado: (1) ENUM [alta, alta]
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("VALUE_DUPLICATED", ex.getCode());
	}

	@Test
	void testUnknownTypeRejected() {
		// STXT-TEMPLATE-SPEC 14.6: the type must be one of the supported ones. In schemas the
		// meta-schema already covered it (Type is an ENUM), but the Structure of a template is a
		// text block and meta-validation does not reach into it.
		String text = """
Template (@stxt.template): test.type.unknown
    Structure >>
        Foo: (1) FOOBAR
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("TYPE_NOT_VALID", ex.getCode());
	}

	@Test
	void testReferenceNotFoundRejected() {
		// STXT-TEMPLATE-SPEC 14.11: a reference must point at a previous definition or at an open
		// ancestor; otherwise a node used to be created whose type was literally '@Otro'
		String text = """
Template (@stxt.template): test.ref.notfound
    Structure >>
        Foo: (1) @Otro
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("REFERENCE_NOT_FOUND", ex.getCode());
	}

	@Test
	void testReferenceWithTypeRejected() {
		// STXT-TEMPLATE-SPEC 14.13: a reference and an explicit type on the same line. It used to
		// be reported as REFERENCE_NAME_NOT_VALID, whose message blamed the name.
		String text = """
Template (@stxt.template): test.ref.withtype
    Structure >>
        Foo: (1) TEXT
        Foo: (2) @Foo TEXT
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("REFERENCE_WITH_TYPE_NOT_ALLOWED", ex.getCode());
	}

	@Test
	void testReferenceToOpenAncestorAllowed() {
		// Recursion through an open ancestor still resolves: by the time the children are walked,
		// the ancestor is already in the schema
		String text = """
Template (@stxt.template): test.ref.recursive
    Structure >>
        Seccion: (1)
            Titulo: (1) TEXT
            Seccion: (*) @Seccion
""";
		Node root = new Parser().parse(text).get(0);
		Schema sch = TemplateParser.transformNodeToSchema(root);
		assertEquals("INLINE", sch.getNodeDefinition("Seccion").getType());
	}

	@Test
	void testReferenceWithSpacesInNameIsNotAType() {
		// Node names may contain spaces: '@Max Threads' is a plain reference, not a reference
		// with a type (the last token is not a known type)
		String text = """
Template (@stxt.template): test.ref.spaces
    Structure >>
        Max Threads: (1) NATURAL
        Max Threads: (?) @Max Threads
""";
		Node root = new Parser().parse(text).get(0);
		Schema sch = TemplateParser.transformNodeToSchema(root);
		assertEquals("NATURAL", sch.getNodeDefinition("Max Threads").getType());
	}

	@Test
	void testStructureLineMustUseInlineForm() {
		String text = """
Template (@stxt.template): test.structure.inline
    Structure >>
        Field >>
""";
		Node root = new Parser().parse(text).get(0);
		ParseException ex = assertThrows(ParseException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("STRUCTURE_LINE_NOT_VALID", ex.getCode());
	}
}
