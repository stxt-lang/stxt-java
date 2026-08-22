package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.runtime.TreeJson;
import test.Corpus;
import test.JSON;

/**
 * The STXT conformance kit: {@code conformance/manifest.json} of stxt-lang lists every case with
 * its category and expected result, so that any implementation can run the same cases with a
 * small runner like this one.
 *
 * <ul>
 * <li>{@code tree}: the input parses and its canonical tree (STXT-TREE-SPEC) equals the expected
 * JSON file, compared as a JSON value.</li>
 * <li>{@code parse-error}: the input is rejected, and the first error carries the expected code
 * and line (STXT-SPEC 11.1).</li>
 * </ul>
 */
public class ConformanceKitTest {

    @TestFactory
    List<DynamicTest> runsTheConformanceKit() {
        File directory = new File(Corpus.findStxtLang(), "conformance");
        JsonNode manifest = JSON.toJsonTree(Corpus.read(new File(directory, "manifest.json")));
        JsonNode cases = manifest.get("cases");
        List<DynamicTest> tests = new ArrayList<>();

        tests.add(dynamicTest("declares a kit version and the specifications it covers", () -> {
            assertTrue(manifest.get("kit").asText().matches("\\d+\\.\\d+"));
            assertEquals("1.0", manifest.get("specifications").get("STXT-SPEC").asText());
            assertEquals("1.0", manifest.get("specifications").get("STXT-TREE-SPEC").asText());
            assertFalse(cases.isEmpty());
        }));

        tests.add(dynamicTest("lists every case file, and every case exactly once", () -> {
            Set<String> ids = new HashSet<>();
            Set<String> listed = new HashSet<>();
            for (JsonNode c: cases) {
                assertTrue(ids.add(c.get("id").asText()), "duplicate case id " + c.get("id").asText());
                listed.add(c.get("input").asText());
            }
            for (String sub: List.of("tree", "parse")) {
                for (File file: Corpus.findStxtFiles(new File(directory, sub))) {
                    assertTrue(listed.contains(sub + "/" + file.getName()), sub + "/" + file.getName() + " is not in the manifest");
                }
            }
        }));

        for (JsonNode c: cases) {
            String id = c.get("id").asText();
            String category = c.get("category").asText();
            String input = Corpus.read(new File(directory, c.get("input").asText()));

            tests.add(dynamicTest(id + ": " + c.get("description").asText(), () -> {
                switch (category) {
                    case "tree": {
                        List<Node> nodes = new Parser().parse(input);
                        JsonNode expected = JSON.toJsonTree(Corpus.read(new File(directory, c.get("expected").asText())));
                        assertEquals(expected, JSON.toJsonTree(TreeJson.toCanonicalJson(nodes)));
                        break;
                    }
                    case "parse-error": {
                        ParseException error = assertThrows(ParseException.class, () -> new Parser().parse(input),
                            id + ": parsed without errors, expected " + c.get("error").get("code").asText());
                        assertNotNull(error);
                        assertEquals(c.get("error").get("code").asText(), error.getCode());
                        assertEquals(c.get("error").get("line").asInt(), error.getLine());
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
