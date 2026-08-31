package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLocation;

public class SchemaProviderResourcesTest {
	@Test
	void testReadSchema() {
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLocation.getFile(""));
		SchemaProvider schemaProvider = new SchemaProviderResources(resourcesLoader);

		Schema sch = schemaProvider.getSchema("com.example.docs");
		assertNotNull(sch, "It should resolve a schema");
	}
}
