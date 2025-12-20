package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import dev.stxt.ParseException;
import dev.stxt.resources.NotFoundException;
import test.JSON;

public class MetaSchemaTest {
	@Test
	void testReadSchema() throws IOException, ParseException, NotFoundException {
		SchemaProviderMeta resolver = new SchemaProviderMeta();
		Schema meta = resolver.getSchema("@stxt.schema");
		assertNotNull(meta, "Debería resolver un schema");
		System.out.println("meta = \n" + JSON.toJsonPretty(meta));
	}

}