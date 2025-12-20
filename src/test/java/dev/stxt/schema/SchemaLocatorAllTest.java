package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import dev.stxt.ParseException;
import dev.stxt.resources.ResourcesException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.utils.FileLoction;

public class SchemaLocatorAllTest {
	@Test
	void testReadSchema() throws IOException, ParseException, ResourcesException {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileLoction.getFileBase(""));
		SchemaProviderCache schemaLocator = new SchemaProviderCache(resourcesLoader);

		File f = FileLoction.getFileBase("@stxt.schema");
		String[] namespaces = f.list();
		for (String namespace : namespaces) {
			checkSchema(namespace, schemaLocator);
		}
	}

	private void checkSchema(String namespace, SchemaProviderCache schemaLocator)
			throws IOException, ParseException, ResourcesException {
		Schema sch = schemaLocator.getSchema("com.example.docs");
		assertNotNull(sch, "Debería resolver un schema");
		System.out.println("SCH => " + sch.toJsonPretty());
	}

}