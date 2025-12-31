package dev.stxt.schema;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileTestLoction;
import test.JSON;
import test.FileChecks;

public class SchemaReaderTestAll {
	@Test
	void testReadSchema() throws Exception {
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));

		// Miramos todos
		File f = FileTestLoction.getFile("@stxt.schema");
		String[] namespaces = f.list();
		for (String namespace : namespaces) {
			checkPrint(namespace, resourcesLoader);
		}
	}

	private void checkPrint(String namespace, ResourcesLoader resolver) throws Exception {
		namespace = namespace.substring(0, namespace.length() - 5);

		System.out.println("*****************************************");
		System.out.println("Namespace: " + namespace);

		String schema = resolver.retrieve("@stxt.schema", namespace);
		try {
			Parser parser = new Parser();
			List<Node> nodes = parser.parse(schema);
			for (Node node : nodes) {
				System.out.println(JSON.toJson(node));
				Schema sch = SchemaParser.transformNodeToSchema(node);
				FileChecks.checkContentWithJsonFile(sch, "schema_json", namespace);
				
				System.out.println("SCH => " + JSON.toJsonPretty(sch));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: " + namespace + " => " + e.getMessage());
			throw e;
		}

	}

}