package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.runtime.STXT;
import dev.stxt.template.MetaTemplateSchemaProvider;
import dev.stxt.template.TemplateSchemaProvider;

/**
 * SchemaProvider contract: providers never throw "not found". {@code getSchema()} returns
 * {@code null} for a namespace they have no schema for, and only {@link SchemaValidator} turns
 * that absence into the {@code SCHEMA_NOT_FOUND} finding. The meta providers used to throw
 * {@code RESOURCE_NOT_FOUND} and the cache a third code, {@code NOT_FOUND_SCHEMA}, so the same
 * situation surfaced with different codes depending on the provider underneath.
 */
public class SchemaProviderContractTest {
	private static final String UNKNOWN = "com.example.unknown";

	// A loader with no resources at all: every retrieve() is a miss
	private static final ResourcesLoader EMPTY = (namespace, resource) -> {
		throw new ResourceNotFoundException(namespace, resource);
	};

	@Test
	public void metaSchemaProviderReturnsNullForOtherNamespaces() {
		SchemaProviderMeta meta = new SchemaProviderMeta();
		assertNull(meta.getSchema(UNKNOWN));
		assertNull(meta.getSchema("@stxt.template"));
		assertNotNull(meta.getSchema("@stxt.schema"));
	}

	@Test
	public void metaTemplateProviderReturnsNullForOtherNamespaces() {
		MetaTemplateSchemaProvider meta = new MetaTemplateSchemaProvider();
		assertNull(meta.getSchema(UNKNOWN));
		assertNull(meta.getSchema("@stxt.schema"));
		assertNotNull(meta.getSchema("@stxt.template"));
	}

	@Test
	public void resourceProvidersReturnNullWhenTheLoaderHasNoResource() {
		assertNull(new SchemaProviderResources(EMPTY).getSchema(UNKNOWN));
		assertNull(new TemplateSchemaProvider(EMPTY).getSchema(UNKNOWN));
	}

	@Test
	public void cacheReturnsNullWhenNoProviderHasTheNamespace() {
		SchemaProviderCache cache = new SchemaProviderCache(List.of(
			new SchemaProviderResources(EMPTY),
			new TemplateSchemaProvider(EMPTY),
			new SchemaProviderMeta()));

		assertNull(cache.getSchema(UNKNOWN));
		assertNotNull(cache.getSchema("@stxt.schema"));
	}

	@Test
	public void validatorReportsSchemaNotFoundAsAFindingWithoutThrowing() {
		SchemaValidator validator = new SchemaValidator(new SchemaProviderMeta(), true);
		Node doc = new Parser().parse("Doc (" + UNKNOWN + "): x\n").get(0);

		List<ValidationException> errors = validator.validate(doc);
		assertEquals(1, errors.size());
		assertEquals("SCHEMA_NOT_FOUND", errors.get(0).getCode());
	}

	@Test
	public void validatorNeverValidatesTheEmptyNamespaceButDoesValidateANamespacedNodeInsideIt() {
		// STXT-SCHEMA-SPEC 5
		SchemaValidator validator = new SchemaValidator(new SchemaProviderMeta(), true);

		assertEquals(0, validator.validate(new Parser().parse("Doc: x\n\tChild: y\n").get(0)).size());

		List<ValidationException> errors = validator.validate(new Parser().parse("Doc: x\n\tFree: y\n\tBound (" + UNKNOWN + "): z\n").get(0));
		assertEquals(1, errors.size());
		assertEquals("SCHEMA_NOT_FOUND", errors.get(0).getCode());
		assertEquals(3, errors.get(0).getLine());
	}

	@Test
	public void facadeReportsSchemaNotFoundForAnUnknownNamespace() {
		ParseResult result = STXT.parser(EMPTY).parseResult("Doc (" + UNKNOWN + "): x\n");

		assertEquals(1, result.getErrors().size());
		assertEquals("SCHEMA_NOT_FOUND", result.getErrors().get(0).getCode());
	}
}
