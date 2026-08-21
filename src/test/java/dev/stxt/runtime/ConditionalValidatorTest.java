package dev.stxt.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.ResourceNotFoundException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.SchemaValidator;

/** {@link ConditionalValidator}: nodes without a namespace are let through, namespaced ones are validated. */
public class ConditionalValidatorTest {
	private static final String TEMPLATE = """
			Template (@stxt.template): com.acme.book
			\tStructure >>
			\t\tBook:
			\t\t\tTitle: (1)
			\t\t\tISBN: (1)
			""";

	private static final String FREE = """
			Note: Hola
			\tText: x
			""";

	private static final String BOUND_OK = """
			Book (com.acme.book):
			\tTitle: T
			\tISBN: 1
			""";

	private static final String BOUND_BAD = """
			Book (com.acme.book):
			\tTitle: T
			""";

	/** In-memory loader with just the template above. */
	private static final ResourcesLoader LOADER = (namespace, resource) -> {
		if (namespace.equals("@stxt.template") && resource.equals("com.acme.book"))
			return TEMPLATE;
		throw new ResourceNotFoundException(namespace, resource);
	};

	@Test
	void freeNodesAreLetThrough() {
		Parser parser = new Parser();
		parser.registerValidator(new ConditionalValidator(new SchemaValidator(STXT.schemaProvider(LOADER))));

		assertTrue(parser.parseResult(FREE).getErrors().isEmpty());
	}

	@Test
	void namespacedNodesAreStillValidated() {
		Parser parser = new Parser();
		parser.registerValidator(new ConditionalValidator(new SchemaValidator(STXT.schemaProvider(LOADER))));

		assertTrue(parser.parseResult(BOUND_OK).getErrors().isEmpty());

		List<ParseException> errors = parser.parseResult(BOUND_BAD).getErrors();
		assertEquals(1, errors.size());
		assertEquals("TOO_FEW_CHILDREN", errors.get(0).getCode());
		assertTrue(errors.get(0).getMessage().endsWith("0 nodes of 'com.acme.book:isbn' and min is 1"));
	}

	@Test
	void mixedDocumentOnlyReportsTheBoundRoot() {
		Parser parser = new Parser();
		parser.registerValidator(new ConditionalValidator(new SchemaValidator(STXT.schemaProvider(LOADER))));

		ParseResult result = parser.parseResult(FREE + BOUND_BAD);
		assertEquals(2, result.getNodes().size());
		assertEquals(1, result.getErrors().size());
		assertEquals(3, result.getErrors().get(0).getLine());
	}

	@Test
	void facadeParserIsConditional() {
		Parser parser = STXT.parser(LOADER);

		assertTrue(parser.parseResult(FREE).getErrors().isEmpty());
		assertEquals(1, parser.parseResult(BOUND_BAD).getErrors().size());
	}

	@Test
	void bareSchemaValidatorLetsFreeNodesThroughToo() {
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(STXT.schemaProvider(LOADER)));

		// Since STXT-SCHEMA-SPEC 5 (2026-08-20) the empty namespace is never validated by the
		// validator itself, so the wrapper is redundant: same result with or without it
		assertTrue(parser.parseResult(FREE).getErrors().isEmpty());
		assertEquals(1, parser.parseResult(FREE + BOUND_BAD).getErrors().size());
	}
}
