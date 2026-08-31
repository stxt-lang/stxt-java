package dev.stxt.runtime;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileUtils;
import test.FileTestLocation;
import test.FileChecks;

public class NodeWriterAllDocsTest {

	@Test
	public void mainTest() throws IOException, ParseException {
		Parser parser = STXT.parser(new ResourcesLoaderDirectory(FileTestLocation.getFile("")));
		File docsDir = FileTestLocation.getFile("docs");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			validateFile(parser, file);
		}
	}

	private void validateFile(Parser parser, File file) throws IOException, ParseException {
		List<Node> docs = parser.parseFile(file);
		for (Node node : docs) {
			String fileName = file.getName().substring(0, file.getName().length() - 5);
			FileChecks.checkContentWithJsonFile(node, "docs_json/", fileName);
			String stxt = NodeWriter.toSTXT(node);
			FileChecks.checkContentWithTextFile(stxt, "docs_txt/", fileName);
		}
	}
}
