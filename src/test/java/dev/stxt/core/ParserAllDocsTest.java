package dev.stxt.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import test.FileChecks;
import test.FileTestLocation;
import test.FileUtils;

public class ParserAllDocsTest {

	@Test
	public void mainTest() throws IOException, ParseException {
		Parser parser = new Parser();
		File docsDir = FileTestLocation.getFile("docs");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			validateFile(parser, file);
		}
	}

	private void validateFile(Parser parser, File file) throws IOException, ParseException {
		List<Node> docs = parser.parseFile(file);
		for (Node node : docs) {
			FileChecks.checkContentWithJsonFile(node, "docs_json/", file.getName().substring(0, file.getName().length() - 5));
		}
	}

}
