package dev.stxt.corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.JsonNode;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.discovery.DiscoveryDefinition;
import dev.stxt.discovery.DiscoveryEntry;
import dev.stxt.discovery.DiscoveryEnvironment;
import dev.stxt.discovery.DiscoveryError;
import dev.stxt.discovery.DiscoveryFileSystem;
import dev.stxt.discovery.DiscoveryResolver;
import dev.stxt.discovery.DiscoveryResult;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.runtime.FormatResult;
import dev.stxt.runtime.Formatter;
import dev.stxt.runtime.NodeWriter;
import dev.stxt.runtime.NodeWriter.IndentStyle;
import dev.stxt.runtime.TreeJson;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderMemory;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.TemplateSchemaProviderMemory;
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
 * <li>{@code validate}: with every set of definitions, the input validates with no error.</li>
 * <li>{@code validate-error}: with every set of definitions, the first validation error carries
 * the expected code and line (STXT-SCHEMA-SPEC 13.1).</li>
 * <li>{@code definition-error}: loading the input as a schema or a template fails with the
 * expected code and line (STXT-SCHEMA-SPEC 13.1, STXT-TEMPLATE-SPEC 14.1).</li>
 * <li>{@code discovery}: a virtual file system and environment resolve to the expected chain,
 * active definitions and resolution errors (STXT-DISCOVERY-SPEC).</li>
 * <li>{@code writer}: the root nodes of the input, written in canonical text form, equal the
 * expected text in both styles (STXT-TREE-SPEC 11).</li>
 * <li>{@code format}: the input reformatted equals the expected text in both styles, with the
 * expected syntax errors (STXT-TREE-SPEC 12).</li>
 * </ul>
 */
public class ConformanceKitTest {

    private static File directory;

    /** In-memory file system rooted at "/" for the discovery cases; paths use '/'. */
    private static final class MemoryFileSystem implements DiscoveryFileSystem {
        private final Map<Path, String> files = new LinkedHashMap<>();
        private final Set<Path> dirs = new HashSet<>();

        MemoryFileSystem() { dirs.add(Path.of("/")); }

        void put(String path, String content) {
            Path file = Path.of(path);
            files.put(file, content);
            addDir(file.getParent());
        }

        void addDir(Path dir) {
            while (dir != null) { dirs.add(dir); dir = dir.getParent(); }
        }

        @Override public boolean isDirectory(Path path) { return dirs.contains(path); }

        @Override public List<DiscoveryEntry> listDirectory(Path path) {
            List<DiscoveryEntry> entries = new ArrayList<>();
            Set<Path> seen = new HashSet<>();
            List<Path> candidates = new ArrayList<>(files.keySet());
            candidates.addAll(dirs);
            for (Path candidate: candidates) {
                if (!candidate.equals(path) && candidate.startsWith(path) && candidate.getNameCount() > path.getNameCount()) {
                    Path child = path.resolve(candidate.getName(path.getNameCount()).toString());
                    if (seen.add(child))
                        entries.add(new DiscoveryEntry(child, child.getFileName().toString(), dirs.contains(child)));
                }
            }
            return entries;
        }

        @Override public String readFile(Path path) throws IOException {
            String content = files.get(path);
            if (content == null) throw new IOException("No such file: " + path);
            return content;
        }
    }

    private static String text(JsonNode node) { return node == null || node.isNull() ? null : node.asText(); }

    private static void discovery(String id, JsonNode c) {
        MemoryFileSystem fs = new MemoryFileSystem();
        c.get("files").fields().forEachRemaining(e -> fs.put(e.getKey(), Corpus.read(new File(directory, e.getValue().asText()))));
        if (c.has("dirs")) c.get("dirs").forEach(d -> fs.addDir(Path.of(d.asText())));
        JsonNode env = c.get("environment");
        List<String> stxtPath = env.get("stxtPath").isNull() ? null : new ArrayList<>();
        if (stxtPath != null) env.get("stxtPath").forEach(p -> stxtPath.add(p.asText()));
        Path userDir = text(env.get("userDir")) == null ? null : Path.of(text(env.get("userDir")));
        Path systemDir = text(env.get("systemDir")) == null ? null : Path.of(text(env.get("systemDir")));
        DiscoveryEnvironment environment = new DiscoveryEnvironment() {
            @Override public List<String> getStxtPath() { return stxtPath; }
            @Override public Path getUserLevelDir() { return userDir; }
            @Override public Path getSystemLevelDir() { return systemDir; }
        };
        String documentDir = text(c.get("documentDir"));
        DiscoveryResult result = new DiscoveryResolver(fs, environment, DiscoveryResolver.DEFAULT_MAX_ASCENT)
            .resolve(documentDir == null ? null : Path.of(documentDir));
        JsonNode expected = c.get("expected");

        List<String> chain = new ArrayList<>();
        expected.get("chain").forEach(p -> chain.add(p.asText()));
        assertEquals(chain, result.getChain().stream().map(p -> p.toString().replace(File.separatorChar, '/')).toList(), id + ": chain");

        expected.get("active").fields().forEachRemaining(e -> {
            DiscoveryDefinition definition = result.getDefinition(e.getKey());
            String file = definition == null ? null : definition.getFile().toString().replace(File.separatorChar, '/');
            assertEquals(text(e.getValue()), file, id + ": active definition of " + e.getKey());
            assertEquals(!e.getValue().isNull(), result.getSchema(e.getKey()) != null, id + ": getSchema(" + e.getKey() + ")");
        });

        List<DiscoveryError> actual = new ArrayList<>(result.getErrors());
        assertEquals(expected.get("errors").size(), actual.size(), id + ": errors " + actual);
        for (JsonNode e: expected.get("errors")) {
            DiscoveryError match = actual.stream().filter(a -> a.getCode().equals(e.get("code").asText())
                && (!e.has("file") || a.getFile().replace(File.separatorChar, '/').equals(e.get("file").asText()))
                && (!e.has("namespace") || e.get("namespace").asText().equals(a.getNamespace()))).findFirst().orElse(null);
            assertNotNull(match, id + ": missing error " + e + " in " + actual);
            actual.remove(match);
        }
    }

    /** A provider holding the given definition files: schemas first, templates on top of them. */
    private static SchemaProvider loadDefinitions(List<String> files, String kind) {
        SchemaProviderMemory schemas = new SchemaProviderMemory();
        TemplateSchemaProviderMemory templates = new TemplateSchemaProviderMemory(schemas);
        for (String file: files) {
            String k = kind != null ? kind : file.endsWith(".schema.stxt") ? "schema" : file.endsWith(".template.stxt") ? "template" : null;
            assertNotNull(k, file + ": a definition file must end in .schema.stxt or .template.stxt");
            String text = Corpus.read(new File(directory, file));
            if (k.equals("schema")) schemas.addSchema(text); else templates.addTemplate(text);
        }
        return templates;
    }

    /** The first validation error of the document against the provider, or null. */
    private static String firstValidationError(String text, SchemaProvider provider) {
        SchemaValidator validator = new SchemaValidator(provider, true);
        for (Node node: new Parser().parse(text)) {
            List<ValidationException> errors = validator.validate(node);
            if (!errors.isEmpty()) return errors.get(0).getCode() + "@" + errors.get(0).getLine();
        }
        return null;
    }

    private static String expected(JsonNode c) {
        return c.get("error").get("code").asText() + "@" + c.get("error").get("line").asInt();
    }

    @TestFactory
    List<DynamicTest> runsTheConformanceKit() {
        directory = new File(Corpus.findStxtLang(), "conformance");
        JsonNode manifest = JSON.toJsonTree(Corpus.read(new File(directory, "manifest.json")));
        JsonNode cases = manifest.get("cases");
        List<DynamicTest> tests = new ArrayList<>();

        tests.add(dynamicTest("declares a kit version and the specifications it covers", () -> {
            assertTrue(manifest.get("kit").asText().matches("\\d+\\.\\d+"));
            assertEquals("1.0", manifest.get("specifications").get("STXT-SPEC").asText());
            assertEquals("1.0", manifest.get("specifications").get("STXT-TREE-SPEC").asText());
            assertFalse(cases.isEmpty());
        }));

        tests.add(dynamicTest("declares cumulative profiles that cover every category", () -> {
            JsonNode profiles = manifest.get("profiles");
            Set<String> covered = new HashSet<>();
            profiles.fields().forEachRemaining(e -> {
                JsonNode p = e.getValue();
                if (p.has("includes")) assertTrue(profiles.has(p.get("includes").asText()), "profile " + e.getKey() + " includes an unknown profile");
                p.get("specifications").forEach(s -> assertTrue(manifest.get("specifications").has(s.asText()), "profile " + e.getKey() + ": unknown specification " + s.asText()));
                p.get("categories").forEach(c -> covered.add(c.asText()));
            });
            for (JsonNode c: cases) assertTrue(covered.contains(c.get("category").asText()), "category " + c.get("category").asText() + " belongs to no profile");
        }));

        tests.add(dynamicTest("lists every case file, and every case exactly once", () -> {
            Set<String> ids = new HashSet<>();
            Set<String> listed = new HashSet<>();
            for (JsonNode c: cases) {
                assertTrue(ids.add(c.get("id").asText()), "duplicate case id " + c.get("id").asText());
                if (c.has("input")) listed.add(c.get("input").asText());
            }
            for (String sub: List.of("tree", "parse", "validate", "definition-errors", "format")) {
                for (File file: Corpus.findStxtFiles(new File(directory, sub))) {
                    if (file.getName().endsWith(".tabs.stxt") || file.getName().endsWith(".spaces.stxt")) continue;
                    assertTrue(listed.contains(sub + "/" + file.getName()), sub + "/" + file.getName() + " is not in the manifest");
                }
            }
        }));

        for (JsonNode c: cases) {
            String id = c.get("id").asText();
            String category = c.get("category").asText();
            String input = c.has("input") ? Corpus.read(new File(directory, c.get("input").asText())) : null;

            tests.add(dynamicTest(id + ": " + c.get("description").asText(), () -> {
                switch (category) {
                    case "discovery":
                        discovery(id, c);
                        break;
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
                    case "validate":
                    case "validate-error": {
                        for (JsonNode set: c.get("definitions")) {
                            List<String> files = new ArrayList<>();
                            set.forEach(f -> files.add(f.asText()));
                            String actual = firstValidationError(input, loadDefinitions(files, null));
                            String where = id + " with " + files;
                            if (category.equals("validate")) assertEquals(null, actual, where);
                            else assertEquals(expected(c), actual, where);
                        }
                        break;
                    }
                    case "writer": {
                        List<Node> nodes = new Parser().parse(input);
                        assertEquals(Corpus.read(new File(directory, c.get("expected").get("tabs").asText())), NodeWriter.toSTXT(nodes, IndentStyle.TABS), id + ": tabs");
                        assertEquals(Corpus.read(new File(directory, c.get("expected").get("spaces").asText())), NodeWriter.toSTXT(nodes, IndentStyle.SPACES_4), id + ": spaces");
                        break;
                    }
                    case "format": {
                        List<String> expectedErrors = new ArrayList<>();
                        c.get("errors").forEach(e -> expectedErrors.add(e.get("code").asText() + "@" + e.get("line").asInt()));
                        for (IndentStyle style: IndentStyle.values()) {
                            String key = style == IndentStyle.TABS ? "tabs" : "spaces";
                            FormatResult result = Formatter.format(input, style);
                            assertEquals(Corpus.read(new File(directory, c.get("expected").get(key).asText())), result.text(), id + ": " + key);
                            assertEquals(expectedErrors, result.errors().stream().map(e -> e.getCode() + "@" + e.getLine()).toList(), id + ": errors with " + key);
                        }
                        break;
                    }
                    case "definition-error": {
                        // Java reports the schema-structure errors as SchemaException, which
                        // carries no line (the kit expects line 0 for them); the rest are
                        // ParseException / ValidationException with their line.
                        STXTException error = assertThrows(STXTException.class,
                            () -> loadDefinitions(List.of(c.get("input").asText()), c.get("kind").asText()),
                            id + ": loaded without errors, expected " + expected(c));
                        int line = error instanceof ParseException pe ? pe.getLine() : 0;
                        assertEquals(expected(c), error.getCode() + "@" + line, id);
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
