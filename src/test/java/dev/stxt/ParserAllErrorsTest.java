package dev.stxt;

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

import dev.stxt.exceptions.ParseException;
import test.FileTestLoction;
import test.JSON;
import test.JSONFile;

public class ParserAllErrorsTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserAllErrorsTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser
		Parser parser = new Parser();
		File docsDir = FileTestLoction.getFile("error_docs");

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
				System.out.println(JSON.toJsonPretty(n));
			Assertions.fail("Expected ParseException for " + file);
		} catch (ParseException e) {
			System.out.println("OK Error at line: " + e.getLine());

			// Build JSON node with line and code from the exception
			Map<String, Object> errorInfo = new HashMap<>();
			errorInfo.put("line", e.getLine());
			errorInfo.put("code", e.getCode());

			JSONFile.checkContentWithJsonFile(errorInfo, "error_docs_json", file.getName().substring(0, file.getName().length() - 5));
		}
	}

}