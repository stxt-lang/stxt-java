package dev.stxt.runtime;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import test.FileChecks;
import test.FileTestLoction;
import test.FileUtils;

/**
 * Cobertura de parseo puro (PENDIENTES.md punto 23): documentos de {@code docs_raw/} no
 * declaran namespace, así que se parsean con {@link STXT#rawParser()} (sin
 * {@code SchemaValidator}: no hay schema que resolver para un namespace vacío). Comprueba el
 * árbol resultante contra {@code docs_raw_json/} y que {@link NodeWriter} reproduce el mismo
 * texto en {@code docs_raw_txt/}.
 */
public class ParserAllRawDocsTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserAllRawDocsTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser (sin validación de schema)
		Parser parser = STXT.rawParser();
		File docsDir = FileTestLoction.getFile("docs_raw");

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
			String fileName = file.getName().substring(0, file.getName().length() - 5);
			FileChecks.checkContentWithJsonFile(node, "docs_raw_json/", fileName);
			System.out.println(node.toString());
			String stxt = NodeWriter.toSTXT(node);
			FileChecks.checkContentWithTextFile(stxt, "docs_raw_txt/", fileName);
		}
	}
}
