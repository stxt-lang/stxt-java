package dev.stxt.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProviderMemory;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.TemplateSchemaProviderMemory;

/** The three in-memory providers (1.0), same contract as in the other ports. */
class MemoryProvidersTest {

	private static final String SCHEMA = """
Schema (@stxt.schema): com.example.blog
	Description >>
		Posts of the blog
	Node: Post
		Children:
			Child: Title
				Min: 1
				Max: 1
	Node: Title
""";

	private static final String TEMPLATE = """
Template (@stxt.template): com.example.tpl
	Structure >>
		Post (com.example.tpl):
			Title: (1)
""";

	private static List<ParseException> errors(String doc, dev.stxt.schema.SchemaProvider provider) {
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(provider));
		return parser.parseResult(doc).getErrors();
	}

	@Test
	void schemaProviderMemoryRegistersValidatesAndFallsBackToTheMetaSchema() {
		SchemaProviderMemory provider = new SchemaProviderMemory();
		provider.addSchema(SCHEMA);

		Schema schema = provider.getSchema("com.example.blog");
		assertNotNull(schema);
		assertEquals("Posts of the blog", schema.getDescription());
		assertNotNull(provider.getSchema("COM.Example.Blog"), "lookup is case-insensitive");
		assertNotNull(provider.getSchema("@stxt.schema"), "falls back to the meta-schema");
		assertNull(provider.getSchema("com.other"));
		assertEquals(1, provider.getAllSchemas().size());

		assertTrue(errors("Post (com.example.blog):\n\tTitle: Hi\n", provider).isEmpty());
		assertEquals("TOO_FEW_CHILDREN", errors("Post (com.example.blog):\n", provider).get(0).getCode());

		provider.clear();
		assertNull(provider.getSchema("com.example.blog"));
	}

	@Test
	void schemaProviderMemoryRejectsWhatDoesNotValidateAgainstTheMetaSchema() {
		SchemaProviderMemory provider = new SchemaProviderMemory();
		assertEquals("SCHEMA_MULTIPLE_ROOTS", assertThrows(ValidationException.class,
			() -> provider.addSchema(SCHEMA + SCHEMA)).getCode());
		assertThrows(ValidationException.class,
			() -> provider.addSchema("Schema (@stxt.schema): com.x\n\tNode: A\n\t\tBogus: 1\n"));
		assertNull(provider.getSchema("com.x"), "an invalid schema is not registered");
	}

	@Test
	void templateSchemaProviderMemoryCompilesTemplatesToSchemas() {
		TemplateSchemaProviderMemory provider = new TemplateSchemaProviderMemory();
		provider.addTemplate(TEMPLATE);

		assertNotNull(provider.getSchema("com.example.tpl"));
		assertNotNull(provider.getSchema("@stxt.template"), "falls back to the template meta-schema");
		assertTrue(errors("Post (com.example.tpl):\n\tTitle: Hi\n", provider).isEmpty());
		assertEquals("TOO_FEW_CHILDREN", errors("Post (com.example.tpl):\n", provider).get(0).getCode());
		assertEquals("TEMPLATE_MULTIPLE_ROOTS", assertThrows(ValidationException.class,
			() -> provider.addTemplate(TEMPLATE + TEMPLATE)).getCode());
	}

	@Test
	void unifiedSchemaProviderLoadsBothKindsAndServesTheMetaSchemas() {
		UnifiedSchemaProvider provider = new UnifiedSchemaProvider();
		provider.addFile(SCHEMA + TEMPLATE + "Free: node\n");

		assertNotNull(provider.getSchema("com.example.blog"));
		assertNotNull(provider.getSchema("com.example.tpl"));
		assertNotNull(provider.getSchema("@stxt.schema"));
		assertNotNull(provider.getSchema("@stxt.template"));
		assertNull(provider.getSchema("free"));
		assertEquals(2, provider.getAllSchemas().size());

		// Schema and template documents validate against it too
		assertTrue(errors(SCHEMA, provider).isEmpty());
		assertTrue(errors(TEMPLATE, provider).isEmpty());
		assertTrue(errors("Post (com.example.tpl):\n\tTitle: Hi\n", provider).isEmpty());

		assertThrows(ValidationException.class,
			() -> provider.addFile("Schema (@stxt.schema): com.bad\n\tNode: A\n\t\tBogus: 1\n"));
		assertNull(provider.getSchema("com.bad"));

		provider.clear();
		assertTrue(provider.getAllSchemas().isEmpty());
		assertNotNull(provider.getSchema("@stxt.schema"), "the meta-schemas survive clear()");
	}
}
