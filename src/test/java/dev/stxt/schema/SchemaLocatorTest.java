package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import dev.stxt.ParseException;
import dev.stxt.resources.ResourcesException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;

public class SchemaLocatorTest {
	@Test
	void testReadSchema() throws IOException, ParseException, ResourcesException {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		SchemaProviderCache schemaLocator = new SchemaProviderCache(resourcesLoader);

		Schema sch = schemaLocator.getSchema("com.example.docs");
		assertNotNull(sch, "Debería resolver un schema");
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}