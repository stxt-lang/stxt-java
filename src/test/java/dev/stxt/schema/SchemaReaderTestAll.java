package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.resources.NotFoundException;
import dev.stxt.resources.ResourcesException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.utils.FileUtils;
import test.FileTestLoction;
import test.JSON;

public class SchemaReaderTestAll {
	@Test
	void testReadSchema() throws IOException, ParseException, ResourcesException {
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));

		// Miramos todos
		File f = FileTestLoction.getFile("@stxt.schema");
		String[] namespaces = f.list();
		for (String namespace : namespaces) {
			checkPrint(namespace, resourcesLoader);
		}
	}

	private void checkPrint(String namespace, ResourcesLoader resolver)
			throws ParseException, IOException, NotFoundException {
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
				
				File json = FileTestLoction.getFile("schema_json/" + namespace + ".json");
				if (json.exists()) {
					String jsonFileContent = FileUtils.readFileContent(json);
					JsonNode treeFile = JSON.toJsonTree(jsonFileContent);
					assertEquals(treeFile.toString(), JSON.toJson(sch));					
				}
				else {
					FileUtils.writeStringToFile(JSON.toJsonPretty(sch), json);
				}				
				
				System.out.println("SCH => " + JSON.toJsonPretty(sch));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: " + namespace + " => " + e.getMessage());
			throw e;
		}

	}

}