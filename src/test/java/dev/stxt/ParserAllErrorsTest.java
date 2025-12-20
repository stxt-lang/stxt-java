package dev.stxt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.utils.FileUtils;
import dev.stxt.utils.JSON;
import test.FileTestLoction;

public class ParserAllErrorsTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserAllErrorsTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser
		Parser parser = new Parser();
		File docsDir = FileTestLoction.getFile("error");

		List<File> stxtFiles;
		try (Stream<Path> stream = Files.walk(docsDir.toPath())) {
			// Discover all .stxt files in test/docs recursively
			stxtFiles = stream.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".stxt"))
					.map(Path::toFile).sorted().collect(Collectors.toList());
		}

		for (File file : stxtFiles) {
			validateFile(parser, file);
		}

		System.out.println("End");
	}

	private void validateFile(Parser parser, File file) throws IOException, ParseException {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(file.getAbsolutePath());

		try {
			List<Node> nodes = parser.parseFile(file);
			for (Node n : nodes)
				System.out.println(n.toJsonPretty());
			Assertions.fail("Expected ParseException for " + file);
		} catch (ParseException e) {
			System.out.println("OK Error at line: " + e.getLine());

			// Build JSON node with line and code from the exception
			Map<String, Object> errorInfo = new HashMap<>();
			errorInfo.put("line", e.getLine());
			errorInfo.put("code", e.getCode());
			JsonNode json = JSON.toJsonTree(JSON.toJson(errorInfo));

			File jsonFile = FileTestLoction.getFile("error_json/" + file.getName().substring(0, file.getName().length() - 5) + ".json");
			if (!jsonFile.exists()) {
				System.out.println("Writting json..." + jsonFile.getAbsolutePath());
				System.out.println(json);
				FileUtils.writeStringToFile(json.toPrettyString(), jsonFile);
			} else {
				System.out.println("Checking json...");
				String jsonFileContent = FileUtils.readFileContent(jsonFile);
				JsonNode treeFile = JSON.toJsonTree(jsonFileContent);
				assertEquals(treeFile.toString(), json.toString());
			}
		}
	}

}