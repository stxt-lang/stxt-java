package dev.stxt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.utils.FileLoction;
import dev.stxt.utils.FileUtils;
import dev.stxt.utils.JSON;

public class ParserAllDocTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserAllDocTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser
		Parser parser = new Parser();
		File docsDir = FileLoction.getFileBase("doc");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			validateFile(parser, file);
		}

		System.out.println("End");
	}

	private void validateFile(Parser parser, File file) throws IOException, ParseException {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(file.getAbsolutePath());
		List<Node> docs = parser.parseFile(file);
		for (Node node : docs) {
			File jsonFile = FileLoction.getFileBase("doc_json/" + file.getName().substring(0, file.getName().length() - 5) + ".json");
			if (!jsonFile.exists()) {
				System.out.println("Writting json..." + jsonFile.getAbsolutePath());
				String json = node.toJsonPretty();
				System.out.println(json);
				FileUtils.writeStringToFile(json, jsonFile);
			} else {
				System.out.println("Checking json...");
				String jsonFileContent = FileUtils.readFileContent(jsonFile);
				JsonNode treeFile = JSON.toJsonTree(jsonFileContent);
				assertEquals(treeFile.toString(), node.toJson());
			}
		}
	}

}