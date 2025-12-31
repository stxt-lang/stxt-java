package dev.stxt.template;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import test.FileChecks;

public class MetaSchemaTest {
	@Test
	void testGetMetaSchems() throws IOException {
		SchemaProvider provider = new MetaTemplateSchemaProvider();
		Schema sch = provider.getSchema("@stxt.template");
		FileChecks.checkContentWithJsonFile(sch, "schema_json", "@stxt.template");
	}
}
