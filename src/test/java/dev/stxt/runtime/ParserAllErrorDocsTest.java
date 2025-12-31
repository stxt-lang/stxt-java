package dev.stxt.runtime;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.resources.ResourcesLoaderDirectory;
import dev.stxt.utils.FileUtils;
import test.FileTestLoction;
import test.FileChecks;

public class ParserAllErrorDocsTest {
	public static void main(String[] args) throws IOException {
		new ParserAllErrorDocsTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException {
		System.out.println("Inici");

		// Create parser
		Parser parser = STXT.parser(new ResourcesLoaderDirectory(FileTestLoction.getFile("")));
		File docsDir = FileTestLoction.getFile("error_schema");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			try {
				System.out.println("***************************************************");
				System.out.println("FILE: " + file.getAbsolutePath());
				parser.parseFile(file);
				fail("Debería haber saltado excepción!!");
			} catch (ParseException e) {
				Map<String, Object> errorInfo = new HashMap<>();
				errorInfo.put("line", e.getLine());
				errorInfo.put("code", e.getCode());
				System.out.println("Error: " + e.getMessage());

				FileChecks.checkContentWithJsonFile(errorInfo, "error_schema_json", file.getName().substring(0, file.getName().length() - 5));
			}
		}

		System.out.println("End");
	}
}