package dev.stxt.template;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.schema.Schema;
import dev.stxt.schema.SchemaProvider;
import test.FileTestLoction;
import test.FileChecks;

public class TemplateSchemaProviderTest {
	@Test
	void testRetrieveAll() throws IOException {
		File mainDir = FileTestLoction.getFile("");
		SchemaProvider schemaProvider = new TemplateSchemaProvider(new ResourcesLoaderDirectory(mainDir));
		
		for (File f: new File(mainDir, "@stxt.template").listFiles()) {
			checkTemplate(schemaProvider, f.getName());
		}
	}

	private void checkTemplate(SchemaProvider schemaProvider, String name) throws IOException {
		assertTrue(name.endsWith(".stxt"));
		name = name.substring(0, name.length()-5);
		
		System.out.println("Checking NS: " + name);
		Schema sch = schemaProvider.getSchema(name);
		assertNotNull(sch);
		
		FileChecks.checkContentWithJsonFile(sch, "schema_json", name);
		System.out.println(sch);
	}
}
