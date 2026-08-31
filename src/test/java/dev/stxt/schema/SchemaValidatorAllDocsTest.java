package dev.stxt.schema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.resources.ResourcesLoaderDirectory;
import test.FileUtils;
import test.FileTestLocation;

public class SchemaValidatorAllDocsTest {

	@Test
	public void mainTest() throws IOException {
		Parser parser = getParser();
		File docsDir = FileTestLocation.getFile("docs");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			try {
				parser.parseFile(file);
			} catch (Exception e) {
				throw new AssertionError("Error validating " + file.getName() + ": " + e.getMessage(), e);
			}
		}
	}

	private Parser getParser() {
		Parser result = new Parser();
		ResourcesLoader resourcesLoader = new ResourcesLoaderDirectory(FileTestLocation.getFile(""));
		SchemaValidator schemaValidator = new SchemaValidator(new SchemaProviderResources(resourcesLoader));

		result.registerValidator(schemaValidator);

		return result;
	}
}
