package dev.stxt.schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;

public class SchemaValidatorTest {
	@Test
	void testReadSchema() throws IOException, ParseException, ResourcesException {
		// Parser file
		File f = FileTestLoction.getFile("docs/email.stxt");
		Parser parser = getParser();
		List<Node> nodes = parser.parseFile(f);

		for (Node n : nodes) {
			System.out.println(JSON.toJsonPretty(n));
		}
	}

	private Parser getParser() throws ResourcesException
	{
		Parser result = new Parser();
		// Path
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		SchemaValidator schemaValidator = new SchemaValidator(new SchemaProviderCache(resourcesLoader));

		result.registerValidator(schemaValidator);
		
		return result;
	}
}