package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.utils.FileUtils;

public class FileChecks {

	public static void checkContentWithJsonFile(Object node, String directory, String name) throws IOException {
		File jsonFile = FileTestLoction.getFile(directory + "/" + name + ".json");
		if (!jsonFile.exists()) {
			System.out.println("Writting json..." + jsonFile.getAbsolutePath());
			String json = JSON.toJsonPretty(node);
			System.out.println(json);
			FileUtils.writeStringToFile(json, jsonFile);
		} else {
			System.out.println("Checking json...");
			String jsonFileContent = FileUtils.readFileContent(jsonFile);
			JsonNode treeFile = JSON.toJsonTree(jsonFileContent);
			assertEquals(treeFile, JSON.toJsonTree(node));
		}
	}
	public static void checkContentWithTextFile(String content, String directory, String name) throws IOException {
		File textFile = FileTestLoction.getFile(directory + "/" + name + ".txt");
		if (!textFile.exists()) {
			System.out.println("Writting text..." + textFile.getAbsolutePath());
			System.out.println(content);
			FileUtils.writeStringToFile(content, textFile);
		} else {
			System.out.println("Checking text...");
			String fileContent = FileUtils.readFileContent(textFile);
			assertEquals(fileContent, JSON.toJsonTree(content));
		}
	}
}
