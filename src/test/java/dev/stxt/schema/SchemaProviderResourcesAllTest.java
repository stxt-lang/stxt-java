package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLocation;

public class SchemaProviderResourcesAllTest {
	@Test
	void testReadSchema() {
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLocation.getFile(""));
		SchemaProvider schemaProvider = new SchemaProviderResources(resourcesLoader);

		File f = FileTestLocation.getFile("@stxt.schema");
		String[] files = f.list();
		assertNotNull(files);
		for (String fileName : files) {
			String namespace = fileName.substring(0, fileName.length() - ".stxt".length());
			assertNotNull(schemaProvider.getSchema(namespace), "It should resolve the schema of " + namespace);
		}
	}
}
