package dev.stxt.schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.resources.ResourcesException;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.utils.FileTestLoction;
import dev.stxt.utils.FileUtils;

public class ParserAllDocsTest {
	public static void main(String[] args) throws IOException, ParseException, ResourcesException {
		new ParserAllDocsTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException, ResourcesException {
		System.out.println("Inici");

		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		DocumentValidator validator = new DocumentValidator(resourcesLoader);

		// Create parser
		Parser parser = new Parser();
		File docsDir = FileTestLoction.getFile("docs");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			try {
				System.out.println("***************************************************");
				System.out.println("FILE: " + file.getAbsolutePath());
				List<Node> docs = parser.parseFile(file);

				for (Node n : docs) {
					if (!n.getNamespace().isEmpty())
						validator.validateNode(n);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR: " + file.getName() + " => " + e.getMessage());
				throw e;
			}
		}

		System.out.println("End");
	}

}