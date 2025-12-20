package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.fail;

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

public class ParserAllDocsErrorTest {
	public static void main(String[] args) throws IOException, ParseException, ResourcesException {
		new ParserAllDocsErrorTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException, ResourcesException {
		System.out.println("Inici");

		// Validator
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		DocumentValidator validator = new DocumentValidator(resourcesLoader);

		// Create parser
		Parser parser = new Parser();
		File docsDir = FileTestLoction.getFile("error_schema");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			try {
				System.out.println("***************************************************");
				System.out.println("FILE: " + file.getAbsolutePath());
				List<Node> docs = parser.parseFile(file);

				validator.validateNodes(docs);
				fail("Debería haber saltado excepción!!");
			} catch (ParseException e) {
				System.out.println("OK Exception: " + e.getMessage());
				System.out.println("Code: " + e.getCode() + ", line: " + e.getLine());
			}
		}

		System.out.println("End");
	}

}