package dev.stxt.schema;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.ParseException;
import dev.stxt.Parser;
import dev.stxt.utils.FileUtils;
import test.FileTestLoction;
import test.JSON;
import test.JSONFile;

public class TemplateParserAllError {
	public static void main(String[] args) throws IOException {
		new TemplateParserAllError().mainTest();
	}

	@Test
	public void mainTest() throws IOException {
		System.out.println("Inici");

		// Create parser
		Parser parser = getParser();
		File docsDir = FileTestLoction.getFile("error_template");

		List<File> stxtFiles = FileUtils.getStxtFiles(docsDir);

		for (File file : stxtFiles) {
			try {
				System.out.println("***************************************************");
				System.out.println("FILE: " + file.getAbsolutePath());
				Node node = parser.parseFile(file).get(0);
				Schema sch = TemplateSchemaParser.transformNodeToSchema(node);
				System.out.println(JSON.toJsonPretty(sch));
				fail("Debería haber saltado excepción!!");
			} catch (ParseException e) {
				// Build JSON node with line and code from the exception
				Map<String, Object> errorInfo = new HashMap<>();
				errorInfo.put("line", e.getLine());
				errorInfo.put("code", e.getCode());
				System.out.println("Error: " + e.getMessage());

				JSONFile.checkContentWithJsonFile(errorInfo, "error_template_json", file.getName().substring(0, file.getName().length() - 5));
			}
		}

		System.out.println("End");
	}
	private Parser getParser()
	{
		Parser result = new Parser();
		return result;
	}
}