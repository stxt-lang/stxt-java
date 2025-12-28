package dev.stxt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.exceptions.ParseException;
import test.FileTestLoction;
import test.JSON;

public class ParserTest {
	public static void main(String[] args) throws IOException, ParseException {
		new ParserTest().mainTest();
	}

	@Test
	public void mainTest() throws IOException, ParseException {
		System.out.println("Inici");

		// Create parser
		Parser parser = createBasicParser();

		File f = FileTestLoction.getFile("docs/client.stxt");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(f.getAbsolutePath());
		List<Node> docs = parser.parseFile(f);
		Node n = docs.get(0);
		System.out.println(JSON.toJson(n)); // System.out.println(n.toJsonPretty());

		System.out.println("End");
	}

	private Parser createBasicParser() {
		Parser parser = new Parser();

		return parser;
	}

}
