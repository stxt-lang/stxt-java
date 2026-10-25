package dev.stxt.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.Parser;
import dev.stxt.exceptions.TransformException;
import dev.stxt.runtime.TreeJson;
import test.Corpus;
import test.FileTestLocation;
import test.JSON;

/**
 * The cases of STXT-TRANSFORM-SPEC (Genesis) in {@code src/test/resources/conformance}, written
 * as data with the layout of the conformance kit of stxt-lang ({@code manifest.json} plus one
 * folder per category), so that they can move to the kit when the specification has normative
 * rules.
 *
 * <ul>
 * <li>{@code fill}: the input, filled with the JSON data, has the canonical tree of the expected
 * document, compared as a JSON value.</li>
 * <li>{@code fill-error}: the fill fails, and the error carries the expected code and line: the
 * line of the node in the input or, with {@code "in": "data"}, the line in the data.</li>
 * </ul>
 */
public class TransformCasesTest {

	private static final File DIRECTORY = FileTestLocation.getFile("conformance");

	private static String read(JsonNode c, String member) {
		return Corpus.read(new File(DIRECTORY, c.get(member).asText()));
	}

	@TestFactory
	List<DynamicTest> runsTheTransformCases() {
		JsonNode cases = JSON.toJsonTree(Corpus.read(new File(DIRECTORY, "manifest.json"))).get("cases");
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(dynamicTest("lists every case file, and every case exactly once", () -> {
			Set<String> ids = new HashSet<>();
			Set<String> listed = new HashSet<>();
			for (JsonNode c: cases) {
				assertTrue(ids.add(c.get("id").asText()), "duplicate case id " + c.get("id").asText());
				listed.add(c.get("input").asText());
			}
			for (File file: Corpus.findStxtFiles(new File(DIRECTORY, "fill"))) {
				if (file.getName().endsWith(".expected.stxt")) continue;
				assertTrue(listed.contains("fill/" + file.getName()), "fill/" + file.getName() + " is not in the manifest");
			}
		}));

		for (JsonNode c: cases) {
			String id = c.get("id").asText();
			String category = c.get("category").asText();

			tests.add(dynamicTest(id + ": " + c.get("description").asText(), () -> {
				String input = read(c, "input");
				String data = read(c, "data");
				switch (category) {
					case "fill": {
						String expected = TreeJson.toCanonicalJson(new Parser().parse(read(c, "expected")));
						String actual = TreeJson.toCanonicalJson(new FillJson().fill(new Parser().parse(input), data));
						assertEquals(JSON.toJsonTree(expected), JSON.toJsonTree(actual), id);
						break;
					}
					case "fill-error": {
						TransformException e = assertThrows(TransformException.class, () -> new FillJson().fill(new Parser().parse(input), data),
								id + ": filled without errors, expected " + c.get("error").get("code").asText());
						assertEquals(c.get("error").get("code").asText() + "@" + c.get("error").get("line").asInt(),
								e.getCode() + "@" + e.getLine(), id + ": " + e.getMessage());
						break;
					}
					default:
						fail(id + ": unknown category " + category);
				}
			}));
		}

		return tests;
	}
}
