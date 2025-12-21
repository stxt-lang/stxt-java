package dev.stxt.schema;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;

public class SchemaParserTest {
	@Test
	void testReadSchema() {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		String schema = resourcesLoader.retrieve("@stxt.schema", "com.example.docs");
		System.out.println("exists: " + schema);

		Parser parser = new Parser();
		List<Node> nodes = parser.parse(schema);
		for (Node node : nodes) {
			System.out.println(JSON.toJson(node));
			Schema sch = SchemaParser.transformNodeToSchema(node);
			checkSchema(sch);
		}
	}

	private void checkSchema(Schema sch) {
		// TODO Auto-generated method stub
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}