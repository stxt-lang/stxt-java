package dev.stxt.schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.utils.FileUtils;
import test.FileTestLoction;

public class ParserAllDocsTest {

	@Test
	public void mainTest() throws IOException {
		System.out.println("Inici");

		// Create parser
		Parser parser = getParser();
		File docsDir = FileTestLoction.getFile("docs");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			try {
				System.out.println("***************************************************");
				System.out.println("FILE: " + file.getAbsolutePath());
				parser.parseFile(file);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR: " + file.getName() + " => " + e.getMessage());
				throw e;
			}
		}

		System.out.println("End");
	}
	private Parser getParser()
	{
		Parser result = new Parser();
		// Path
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLoction.getFile(""));
		SchemaValidator schemaValidator = new SchemaValidator(new SchemaProviderResources(resourcesLoader));

		result.registerValidator(schemaValidator);
		
		return result;
	}
	

}