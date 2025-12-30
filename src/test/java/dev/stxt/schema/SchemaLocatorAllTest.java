package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;

public class SchemaLocatorAllTest {
	@Test
	void testReadSchema(){
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		SchemaProvider schemaLocator = new SchemaProviderResources(resourcesLoader);

		File f = FileTestLoction.getFile("@stxt.schema");
		String[] namespaces = f.list();
		for (String namespace : namespaces) {
			checkSchema(namespace, schemaLocator);
		}
	}

	private void checkSchema(String namespace, SchemaProvider schemaLocator) {
		Schema sch = schemaLocator.getSchema("com.example.docs");
		assertNotNull(sch, "Debería resolver un schema");
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}