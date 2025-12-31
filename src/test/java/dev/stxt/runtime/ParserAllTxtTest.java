package dev.stxt.runtime;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileChecks;
import test.FileTestLoction;

public class ParserAllTxtTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserAllTxtTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser
		Parser parser = STXT.parser(new ResourcesLoaderDirectory(FileTestLoction.getFile("")));
		File docsDir = FileTestLoction.getFile("docs_txt");
		for (File file : docsDir.listFiles()) {
			validateFile(parser, file);
		}

		System.out.println("End");
	}

	private void validateFile(Parser parser, File file) throws IOException, ParseException {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(file.getAbsolutePath());
		List<Node> docs = parser.parseFile(file);
		for (Node node : docs) {
			String fileName = file.getName().substring(0, file.getName().length() - 4);
			String stxt = NodeWriter.toSTXT(node);
			FileChecks.checkContentWithTextFile(stxt, "docs_txt/", fileName);
		}
	}
}