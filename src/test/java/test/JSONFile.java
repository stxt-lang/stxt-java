package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.utils.FileUtils;

public class JSONFile {

	public static void checkContentWithJsonFile(Object node, String string) throws IOException {
		File jsonFile = FileTestLoction.getFile(string + ".json");
		if (!jsonFile.exists()) {
			System.out.println("Writting json..." + jsonFile.getAbsolutePath());
			String json = JSON.toJsonPretty(node);
			System.out.println(json);
			FileUtils.writeStringToFile(json, jsonFile);
		} else {
			System.out.println("Checking json...");
			String jsonFileContent = FileUtils.readFileContent(jsonFile);
			JsonNode treeFile = JSON.toJsonTree(jsonFileContent);
			assertEquals(treeFile.toString(), JSON.toJson(node));
		}
	}
}
