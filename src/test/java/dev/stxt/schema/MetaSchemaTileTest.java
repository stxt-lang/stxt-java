package dev.stxt.schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.utils.FileUtils;
import test.FileTestLoction;
import test.JSON;
import test.JSONFile;

public class MetaSchemaTileTest {
	@Test
	void testReadSchema() throws IOException {
		// Validator
		File metaSchemaTest = FileTestLoction.getFile("@stxt.schema.stxt");
		String schema = FileUtils.readFileContent(metaSchemaTest);

		Parser parser = new Parser();
		List<Node> nodes = parser.parse(schema);
		for (Node node : nodes) {
			System.out.println(JSON.toJson(node));
			Schema sch = SchemaParser.transformNodeToSchema(node);
			showSchema(sch);
			JSONFile.checkContentWithJsonFile(sch, "schema_json", "@stxt.schema");
		}
	}

	private void showSchema(Schema sch) {
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}