package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.runtime.STXT;
import dev.stxt.template.TemplateParser;
import dev.stxt.template.TemplateSchemaProvider;

/**
 * The error codes introduced or split on 2026-08-21 (STXT-SCHEMA-SPEC 13.1, STXT-TEMPLATE-SPEC 14.1):
 * every code has the condition of the annex of {@code stxt-impl/exceptions/exceptions.txt} and,
 * for the ones that are a {@link ValidationException}, the line the annex asks for.
 */
public class ErrorCodesTest {

	private static final String SCHEMA = """
			Schema (@stxt.schema): com.acme.book
			\tNode: Book
			\t\tChildren:
			\t\t\tChild: Title
			\t\t\t\tMin: 1
			\t\t\t\tMax: 1
			\t\t\tChild: Tags
			\tNode: Title
			\tNode: Tags
			\t\tType: GROUP
			""";

	private static ResourcesLoader loader(String kind, String namespace, String text) {
		return (ns, resource) -> {
			if (ns.equals(kind) && resource.equals(namespace))
				return text;
			throw new ResourceNotFoundException(ns, resource);
		};
	}

	private static List<ParseException> validate(String document) {
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(STXT.schemaProvider(loader("@stxt.schema", "com.acme.book", SCHEMA))));
		return parser.parseResult(document).getErrors();
	}

	private static ValidationException schemaError(String schemaText) {
		SchemaProvider provider = new SchemaProviderResources(loader("@stxt.schema", "com.acme.book", schemaText));
		return assertThrows(ValidationException.class, () -> provider.getSchema("com.acme.book"));
	}

	private static ValidationException templateError(String templateText) {
		SchemaProvider provider = new TemplateSchemaProvider(loader("@stxt.template", "com.acme.book", templateText));
		return assertThrows(ValidationException.class, () -> provider.getSchema("com.acme.book"));
	}

	// ---- document validation -------------------------------------------------------------

	@Test
	void tooFewChildrenWhenCountIsBelowMin() {
		List<ParseException> errors = validate("Book (com.acme.book):\n");
		assertEquals(1, errors.size());
		assertEquals("TOO_FEW_CHILDREN", errors.get(0).getCode());
		assertEquals(1, errors.get(0).getLine());
	}

	@Test
	void tooManyChildrenWhenCountIsAboveMax() {
		List<ParseException> errors = validate("Book (com.acme.book):\n\tTitle: a\n\tTitle: b\n");
		assertEquals(1, errors.size());
		assertEquals("TOO_MANY_CHILDREN", errors.get(0).getCode());
	}

	@Test
	void valueNotAllowedOnAGroup() {
		List<ParseException> errors = validate("Book (com.acme.book):\n\tTitle: a\n\tTags: x\n");
		assertEquals(1, errors.size());
		assertEquals("VALUE_NOT_ALLOWED", errors.get(0).getCode());
		assertEquals(3, errors.get(0).getLine());
	}

	// ---- schema load ----------------------------------------------------------------------

	@Test
	void valuesDuplicatedIsAValidationExceptionAtTheSecondValuesNode() {
		String text = """
				Schema (@stxt.schema): com.acme.book
				\tNode: Book
				\t\tType: ENUM
				\t\tValues:
				\t\t\tValue: a
				\t\tValues:
				\t\t\tValue: b
				""";
		Node root = new Parser().parse(text).get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
		assertEquals("VALUES_DUPLICATED", ex.getCode());
		assertEquals(6, ex.getLine());
	}

	private static ValidationException schemaParserError(String schemaText) {
		Node root = new Parser().parse(schemaText).get(0);
		return assertThrows(ValidationException.class, () -> SchemaParser.transformNodeToSchema(root));
	}

	@Test
	void schemaMultipleRoots() {
		// Two well-formed schema roots: the meta-schema accepts each one, the loader rejects the pair.
		// A document-level condition carries no single line: ParseException.NO_LINE (0), like the
		// other ports and the conformance kit.
		ValidationException ex = schemaError(SCHEMA + SCHEMA.replace("com.acme.book", "com.acme.other"));
		assertEquals("SCHEMA_MULTIPLE_ROOTS", ex.getCode());
		assertEquals(ParseException.NO_LINE, ex.getLine());
	}

	@Test
	void schemaNamespaceEmpty() {
		ValidationException ex = schemaError("Schema (@stxt.schema):\n\tNode: Book\n");
		assertEquals("SCHEMA_NAMESPACE_EMPTY", ex.getCode());
		assertEquals(1, ex.getLine());
	}

	@Test
	void schemaRootNotValidWhenTheRootIsNotSchema() {
		ValidationException ex = schemaParserError("Esquema (@stxt.schema): com.acme.book\n");
		assertEquals("SCHEMA_ROOT_NOT_VALID", ex.getCode());
		assertEquals(1, ex.getLine());
	}

	@Test
	void schemaRootNotValidWhenTheTargetNamespaceIsMalformed() {
		ValidationException ex = schemaParserError("Schema (@stxt.schema): not a namespace\n");
		assertEquals("SCHEMA_ROOT_NOT_VALID", ex.getCode());
		assertEquals(1, ex.getLine());
	}

	@Test
	void schemaRootNotValidWhenTheTargetNamespaceIsNotTheRequestedOne() {
		// A facade check of the whole document against the requested namespace: no single line
		// to point at, so ParseException.NO_LINE (0) since the shared DefinitionCompiler pipeline.
		ValidationException ex = schemaError(SCHEMA.replace("com.acme.book", "com.acme.other"));
		assertEquals("SCHEMA_ROOT_NOT_VALID", ex.getCode());
		assertEquals(ParseException.NO_LINE, ex.getLine());
	}

	@Test
	void schemaNodeNotInline() {
		ValidationException ex = schemaParserError("Schema (@stxt.schema): com.acme.book\n\tNode >>\n\t\tBook\n");
		assertEquals("SCHEMA_NODE_NOT_INLINE", ex.getCode());
		assertEquals(2, ex.getLine());
	}

	// ---- template load --------------------------------------------------------------------

	@Test
	void templateRootNotValidWhenTheRootIsNotTemplate() {
		String text = "Plantilla (@stxt.template): com.acme.book\n\tStructure >>\n\t\tBook:\n";
		Node root = new Parser().parse(text).get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> TemplateParser.transformNodeToSchema(root));
		assertEquals("TEMPLATE_ROOT_NOT_VALID", ex.getCode());
		assertEquals(1, ex.getLine());
	}

	@Test
	void templateRootNotValidWhenTheTargetNamespaceIsMalformed() {
		ValidationException ex = templateError("Template (@stxt.template): not a namespace\n\tStructure >>\n\t\tBook:\n");
		assertEquals("TEMPLATE_ROOT_NOT_VALID", ex.getCode());
	}

	@Test
	void templateNamespaceEmpty() {
		ValidationException ex = templateError("Template (@stxt.template):\n\tStructure >>\n\t\tBook:\n");
		assertEquals("TEMPLATE_NAMESPACE_EMPTY", ex.getCode());
		assertEquals(1, ex.getLine());
	}

	@Test
	void templateMultipleRoots() {
		// Same document-level rule as SCHEMA_MULTIPLE_ROOTS: ParseException.NO_LINE (0)
		String one = "Template (@stxt.template): com.acme.book\n\tStructure >>\n\t\tBook:\n";
		ValidationException ex = templateError(one + one);
		assertEquals("TEMPLATE_MULTIPLE_ROOTS", ex.getCode());
		assertEquals(ParseException.NO_LINE, ex.getLine());
	}
}
