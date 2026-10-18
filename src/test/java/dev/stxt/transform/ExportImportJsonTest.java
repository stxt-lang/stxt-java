package dev.stxt.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.exceptions.TransformException;
import dev.stxt.runtime.TreeJson;
import dev.stxt.runtime.UnifiedSchemaProvider;
import dev.stxt.schema.SchemaValidator;

/**
 * The export and the import of STXT-TRANSFORM-SPEC §3 (Genesis), with the example of the
 * specification and the cases around it. Error codes are provisional while the specification is
 * in Genesis.
 */
public class ExportImportJsonTest {

	private static String doc(String... lines) {
		return String.join("\n", lines);
	}

	// The template of §3 (en/), with two optional children more: Load and the repeatable Tag
	private static final String DEFINITION = doc(
			"Template (@stxt.template): com.example.servers",
			"\tStructure >>",
			"\t\tServer (com.example.servers): GROUP",
			"\t\t\tName: (1)",
			"\t\t\tActive: (1) BOOLEAN",
			"\t\t\tPorts: (1) GROUP",
			"\t\t\t\tPort: (+) NATURAL",
			"\t\t\tOwner: (?) GROUP",
			"\t\t\t\tPerson: (1)",
			"\t\t\t\tEmail: (1) EMAIL",
			"\t\t\tNotes: (?) TEXT",
			"\t\t\tLoad: (?) NUMBER",
			"\t\t\tTag: (*)");

	// The document of §3 (en/)
	private static final String DOCUMENT = doc(
			"# The web server of the office",
			"Server (com.example.servers):",
			"\tName: web-01",
			"\tActive: true",
			"\tPorts:",
			"\t\tPort: 80",
			"\t\tPort: 443",
			"\tOwner:",
			"\t\tPerson: Ana Ruiz",
			"\t\tEmail: ana@example.com",
			"\tNotes >>",
			"\t\tRestart after every deployment.",
			"\t\tDo not switch off during office hours.");

	// Its export, as §3.1 shows it
	private static final String EXPORTED = doc(
			"{",
			"\t\"Name\": \"web-01\",",
			"\t\"Active\": true,",
			"\t\"Ports\": [80, 443],",
			"\t\"Owner\": {",
			"\t\t\"Person\": \"Ana Ruiz\",",
			"\t\t\"Email\": \"ana@example.com\"",
			"\t},",
			"\t\"Notes\": \"Restart after every deployment.\\nDo not switch off during office hours.\"",
			"}");

	private static final String NS = "com.example.servers";

	private final UnifiedSchemaProvider schemas = new UnifiedSchemaProvider();

	ExportImportJsonTest() {
		schemas.addFile(DEFINITION);
	}

	private static Node parse(String document) {
		return new Parser().parse(document).get(0);
	}

	private String export(String document) {
		return new ExportJson(schemas).exportJson(parse(document));
	}

	private Node importServer(String json) {
		return new ImportJson(schemas).importJson(json, "Server", NS);
	}

	private boolean valid(Node node) {
		return new SchemaValidator(schemas, true).validate(node).isEmpty();
	}

	private static void assertFails(String code, String message, Executable transform) {
		TransformException e = assertThrows(TransformException.class, transform);
		assertEquals(code, e.getCode(), e.toString());
		assertTrue(e.getMessage().contains(message), e.toString());
	}

	// ----------------------------------------------------------------
	// The example of §3
	// ----------------------------------------------------------------

	@Test
	void exportsTheExampleAsTheSpecificationShowsIt() {
		assertEquals(EXPORTED, export(DOCUMENT));
	}

	@Test
	void importsTheExampleBackWithTheSameTree() {
		Node imported = importServer(EXPORTED);
		assertTrue(valid(imported));
		assertEquals(TreeJson.toCanonicalJson(parse(DOCUMENT)), TreeJson.toCanonicalJson(imported));
	}

	// ----------------------------------------------------------------
	// Export
	// ----------------------------------------------------------------

	@Test
	void exportsNumbersWithTheirValueAndRepeatableChildrenAsArrays() {
		assertEquals(doc(
				"{",
				"\t\"Name\": \"x\",",
				"\t\"Active\": false,",
				"\t\"Ports\": [7],",
				"\t\"Load\": 0.50,",
				"\t\"Tag\": [\"a\", \"b\"]",
				"}"),
				export(doc("Server (com.example.servers):", "\tTag: a", "\tName: x", "\tActive: false", "\tPorts:", "\t\tPort: 007",
						"\tLoad: +.50", "\tTag: b")));
	}

	@Test
	void exportsABlockAsAStringWithLineFeeds() {
		assertEquals("\"one\\n  two\"", new ExportJson(schemas).exportJson(parse(doc("Notes (com.example.servers) >>", "\tone", "\t  two"))));
	}

	@Test
	void anExportNeedsTheSchemaOfTheNamespace() {
		assertFails("SCHEMA_NOT_FOUND", "com.example.other", () -> export("Server (com.example.other):"));
		assertFails("NODE_NOT_DEFINED_IN_SCHEMA", "'Client'", () -> export("Client (com.example.servers):"));
	}

	@Test
	void anExportOfAnInvalidValueIsAnError() {
		assertFails("INVALID_VALUE", "Port: not a valid NATURAL (eighty)",
				() -> export(doc("Server (com.example.servers):", "\tPorts:", "\t\tPort: eighty")));
	}

	@Test
	void aNodeWithAValueAndChildrenIsNotDecidedYet() {
		assertFails("EXPORT_VALUE_AND_CHILDREN", "Ports",
				() -> export(doc("Server (com.example.servers):", "\tPorts: two", "\t\tPort: 80")));
	}

	// ----------------------------------------------------------------
	// Import
	// ----------------------------------------------------------------

	@Test
	void acceptsAStringANumberOrABooleanForAnyLeafAndTheValidationJudges() {
		Node imported = importServer("{\"Name\": 1.10, \"Active\": \"true\", \"Ports\": [\"80\", 443]}");
		assertEquals(TreeJson.toCanonicalJson(parse(doc("Server (com.example.servers):", "\tName: 1.10", "\tActive: true",
				"\tPorts:", "\t\tPort: 80", "\t\tPort: 443"))), TreeJson.toCanonicalJson(imported));
		assertTrue(valid(imported));
		assertFalse(valid(importServer("{\"Name\": true, \"Active\": \"yes\", \"Ports\": [\"abc\"]}")));
	}

	@Test
	void matchesKeysByCanonicalName() {
		Node imported = importServer("{\"name\": \"x\", \" ACTIVE \": true, \"ports\": [1]}");
		assertEquals(TreeJson.toCanonicalJson(parse(doc("Server (com.example.servers):", "\tName: x", "\tActive: true",
				"\tPorts:", "\t\tPort: 1"))), TreeJson.toCanonicalJson(imported));
	}

	@Test
	void choosesTheFormThatLosesLess() {
		assertInstanceOf(InlineNode.class, ((InlineNode) importServer("{\"Notes\": \"one line\"}")).getChildren().get(0));
		assertInstanceOf(TextNode.class, ((InlineNode) importServer("{\"Notes\": \"a\\nb\"}")).getChildren().get(0));
		assertInstanceOf(TextNode.class, ((InlineNode) importServer("{\"Notes\": \"    indented\"}")).getChildren().get(0));
		// Tag has no type, and its type has only the inline form: a text with lines is a block, and
		// the validation rejects it
		Node tag = importServer("{\"Name\": \"x\", \"Active\": true, \"Ports\": [1], \"Tag\": [\"a\\nb\"]}");
		assertInstanceOf(TextNode.class, ((InlineNode) tag).getChildren().get(3));
		assertFalse(valid(tag));
	}

	@Test
	void anImportOfDataWithoutTheShapeOfItsNodeIsAnError() {
		assertFails("IMPORT_DATA_MISMATCH", "'Ports' expects an array, not a number", () -> importServer("{\"Ports\": 80}"));
		assertFails("IMPORT_DATA_MISMATCH", "'Owner' expects an object, not an array", () -> importServer("{\"Owner\": []}"));
		assertFails("IMPORT_DATA_MISMATCH", "'Name' expects a string, a number or a boolean, not an object",
				() -> importServer("{\"Name\": {}}"));
		assertFails("IMPORT_DATA_MISMATCH", "'Name' expects a string, a number or a boolean, not null",
				() -> importServer("{\"Name\": null}"));
		assertFails("IMPORT_DATA_MISMATCH", "'Tag' expects an array, not a string", () -> importServer("{\"Tag\": \"a\"}"));
		assertFails("IMPORT_DATA_MISMATCH", "'Server' expects an object, not an array", () -> importServer("[]"));
	}

	@Test
	void aKeyThatIsNotAChildIsAnError() {
		assertFails("IMPORT_KEY_NOT_DEFINED", "Key 'Host' is not a child of 'Server'", () -> importServer("{\"Host\": \"x\"}"));
	}

	@Test
	void anImportNeedsTheSchemaOfTheNamespace() {
		assertFails("SCHEMA_NOT_FOUND", "com.example.other", () -> new ImportJson(schemas).importJson("{}", "Server", "com.example.other"));
		assertFails("NODE_NOT_DEFINED_IN_SCHEMA", "'Client'", () -> new ImportJson(schemas).importJson("{}", "Client", NS));
	}

	@Test
	void anImportOfTextThatIsNotJsonIsAnError() {
		assertFails("JSON_NOT_VALID", "Expected a key", () -> importServer("{'Name': 'x'}"));
		assertFails("JSON_DUPLICATE_KEY", "\"Name\"", () -> importServer("{\"Name\": \"x\", \"Name\": \"y\"}"));
	}
}
