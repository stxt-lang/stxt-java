package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLocation;

public class StxtParserTest {
	@Test
	void testParser() {
		Parser parser = new Parser();

		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLocation.getFile(""));
		SchemaProvider schemaProvider = new SchemaProviderResources(resourcesLoader);
		SchemaValidator schemaValidator = new SchemaValidator(schemaProvider);
		parser.registerValidator(schemaValidator);

		File f = FileTestLocation.getFile("docs/client.stxt");
		List<Node> docs = parser.parseFile(f);
		assertFalse(docs.isEmpty(), "It should parse and validate the document");
	}
}
