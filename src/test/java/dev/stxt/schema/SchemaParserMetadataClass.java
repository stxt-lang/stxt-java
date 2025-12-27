package dev.stxt.schema;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import test.JSON;
import test.JSONFile;

public class SchemaParserMetadataClass {
	@Test
	void testReadSchema() throws IOException {
		// Validator
		SchemaProvider meta = new SchemaProviderMeta();
		Schema sch = meta.getSchema("@stxt.schema");
		showSchema(sch);
		JSONFile.checkContentWithJsonFile(sch, "schema_json", "@stxt.schema");
	}

	private void showSchema(Schema sch) {
		System.out.println("SCH => " + JSON.toJsonPretty(sch));
	}

}