package dev.stxt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.exceptions.ParseException;
import dev.stxt.utils.FileUtils;
import test.FileTestLoction;
import test.JSONFile;

public class ParserAllDocsTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserAllDocsTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser
		Parser parser = new Parser();
		File docsDir = FileTestLoction.getFile("docs");

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
			JSONFile.checkContentWithJsonFile(node, "docs_json/", file.getName().substring(0, file.getName().length() - 5));
		}
	}

}