package dev.stxt.schema;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;
import test.JSONFile;

public class TemplateToSchemaTest {
	@Test
	void testReadSchema() throws IOException {
		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		String schema = resourcesLoader.retrieve("@stxt.template", "com.example.docs");
		System.out.println("exists: " + schema);
		System.out.println("==========================================================");
		Parser parser = new Parser();
		List<Node> nodes = parser.parse(schema);
		for (Node node : nodes) {
			//System.out.println(JSON.toJsonPretty(node));
			Schema sch = TemplateSchemaParser.transformNodeToSchema(node);
			showSchema(sch);
			JSONFile.checkContentWithJsonFile(sch, "schema_json", "com.example.docs");
		}
	}

	private void showSchema(Schema sch) {
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}