package dev.stxt.schema;

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

public class SchemaParserTest {
	@Test
	void testReadSchema() throws IOException, ParseException, ResourcesException {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		String schema = resourcesLoader.retrieve("@stxt.schema", "com.example.docs");
		System.out.println("exists: " + schema);

		Parser parser = new Parser();
		List<Node> nodes = parser.parse(schema);
		for (Node node : nodes) {
			System.out.println(node.toJson());
			Schema sch = SchemaParser.transformNodeToSchema(node);
			checkSchema(sch);
		}
	}

	private void checkSchema(Schema sch) {
		// TODO Auto-generated method stub
		System.out.println("SCH => " + sch.toJsonPretty());
	}

}