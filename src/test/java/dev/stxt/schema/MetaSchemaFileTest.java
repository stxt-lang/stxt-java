package dev.stxt.schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.utils.FileUtils;
import test.FileTestLocation;
import test.FileChecks;

public class MetaSchemaFileTest {
	@Test
	void testReadSchema() throws IOException {
		File metaSchemaFile = FileTestLocation.getFile("@stxt.schema.stxt");
		String schema = FileUtils.readFileContent(metaSchemaFile);

		Parser parser = new Parser();
		List<Node> nodes = parser.parse(schema);
		for (Node node : nodes) {
			Schema sch = SchemaParser.transformNodeToSchema(node);
			FileChecks.checkContentWithJsonFile(sch, "schema_json", "@stxt.schema");
		}
	}
}
