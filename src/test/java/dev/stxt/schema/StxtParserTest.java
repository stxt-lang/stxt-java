package dev.stxt.schema;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;

public class StxtParserTest {
	@Test
	void testParser() {
		System.out.println("Inici");
		
		Parser parser = new Parser();
		
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		SchemaProviderCache schemaLocator = new SchemaProviderCache(resourcesLoader);
		SchemaValidator schemaValidator = new SchemaValidator(schemaLocator);
		parser.registerValidator(schemaValidator);
		
		File f = FileTestLoction.getFile("docs/client.stxt");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(f.getAbsolutePath());
		List<Node> docs = parser.parseFile(f);
		for (Node doc: docs)
			System.out.println(JSON.toJsonPretty(doc));
	}	
	
}
