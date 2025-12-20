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
import dev.stxt.utils.FileLoction;

public class SchemaValidatorTest {
	@Test
	void testReadSchema() throws IOException, ParseException, ResourcesException {
		// Path
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory("./test");

		// Validator
		DocumentValidator schemaValidator = new DocumentValidator(resourcesLoader);

		// Parser file
		File f = FileLoction.getFileBase("docs/email.stxt");
		Parser parser = new Parser();
		List<Node> nodes = schemaValidator.validateNodes(parser.parseFile(f));

		for (Node n : nodes) {
			System.out.println(n.toJsonPretty());
		}
	}

}