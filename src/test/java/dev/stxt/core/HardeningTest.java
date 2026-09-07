package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.discovery.DiscoveryEntry;
import dev.stxt.discovery.DiscoveryEnvironment;
import dev.stxt.discovery.DiscoveryError;
import dev.stxt.discovery.DiscoveryFileSystem;
import dev.stxt.discovery.DiscoveryResolver;
import dev.stxt.discovery.DiscoveryResult;
import dev.stxt.discovery.NioDiscoveryFileSystem;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.STXTException;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.runtime.UnifiedSchemaProvider;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.ChildLine;
import dev.stxt.template.ChildLineParser;
import dev.stxt.utils.StringUtils;

/**
 * Hardening of the 2026-09-06 security review: every case here reproduced a pathological cost,
 * an escaping error or a structure injection before the fix. Mirrors hardening.test.ts of the
 * TypeScript port and test_hardening.py of the Python port.
 */
public class HardeningTest {

	@TempDir
	Path tempDir;

	private static final DiscoveryEnvironment NO_ENV = new DiscoveryEnvironment() {
		@Override public List<String> getStxtPath() { return null; }
		@Override public Path getUserLevelDir() { return null; }
		@Override public Path getSystemLevelDir() { return null; }
	};

	// ---------------------------------------------------------------- template RuleSpec

	@Test
	void childLineRejectsAnUnclosedListOfBlanksInMilliseconds() {
		for (String line : List.of("TEXT [" + " ".repeat(9990) + "x", "[" + "\t ".repeat(4995) + "x", "(" + " ".repeat(9990) + "x")) {
			long start = System.nanoTime();
			ValidationException e = assertThrows(ValidationException.class, () -> ChildLineParser.parse(line, 1));
			assertEquals("STRUCTURE_LINE_NOT_VALID", e.getCode());
			assertTrue(System.nanoTime() - start < 1_000_000_000L, "was minutes");
		}
	}

	@Test
	void childLineKeepsTheGrammarOfTheFormerPattern() {
		ok("( 2 ) TEXT", "TEXT", 2L, 2L, null);
		ok("  [a, b]  ", null, null, null, new String[] { "a", "b" });
		ok("TEXT [ ]", "TEXT", null, null, new String[] {});
		ok("(1,3) ENUM [ a , b ]", "ENUM", 1L, 3L, new String[] { "a", "b" });
		ok("TEXT [a[b]", "TEXT", null, null, new String[] { "a[b" });
		ok("(?)\t@Ref\t", "@Ref", null, 1L, null);

		for (String line : List.of("()", "( )", "((1))", "(1", "TEXT (1)", "(1) (2)", "TEXT a]", "TEXT [a", "[a] b", "[a] [b]", "[a]]"))
			ko(line, "STRUCTURE_LINE_NOT_VALID");
		ko("(1[) TEXT", "CARDINALITY_NOT_VALID");
		ko("(x) TEXT", "CARDINALITY_NOT_VALID");
		ko("(" + "1".repeat(5000) + ") TEXT", "CARDINALITY_NOT_VALID");
	}

	private static void ok(String line, String type, Long min, Long max, String[] values) {
		ChildLine c = ChildLineParser.parse(line, 1);
		assertEquals(type, c.getType(), line);
		assertEquals(min, c.getMin(), line);
		assertEquals(max, c.getMax(), line);
		if (values == null)		assertNull(c.getValues(), line);
		else					assertEquals(List.of(values), List.of(c.getValues()), line);
	}

	private static void ko(String line, String expected) {
		ValidationException e = assertThrows(ValidationException.class, () -> ChildLineParser.parse(line, 1), line);
		assertEquals(expected, e.getCode(), line);
	}

	// ---------------------------------------------------------------- ENUM message

	@Test
	void anEnumInvalidValueMessageDoesNotCarryTheListOfValues() {
		StringBuilder values = new StringBuilder();
		for (int i = 0; i < 2000; i++)
			values.append("\t\t\tValue: v").append(i).append('\n');
		UnifiedSchemaProvider provider = new UnifiedSchemaProvider();
		provider.addFile("Schema (@stxt.schema): com.example.big\n\tNode: R\n\t\tChildren:\n\t\t\tChild: E\n\t\t\t\tMax: 5\n\tNode: E\n\t\tType: ENUM\n\t\tValues:\n" + values);
		Parser parser = new Parser();
		parser.registerValidator(new SchemaValidator(provider, false));

		ParseResult result = parser.parseResult("R (com.example.big):\n\tE: zz\n\tE: v1\n");

		assertEquals(1, result.getErrors().size());
		assertEquals("INVALID_VALUE", result.getErrors().get(0).getCode());
		assertTrue(result.getErrors().get(0).getMessage().length() < 100, result.getErrors().get(0).getMessage());
		assertTrue(result.getErrors().get(0).getMessage().contains("'zz'"));
	}

	// ---------------------------------------------------------------- line breaks through the API

	@Test
	void inlineNodeRejectsLfAndKeepsALoneCr() {
		assertEquals("LINE_BREAK_NOT_ALLOWED", assertThrows(STXTException.class, () -> new InlineNode("A", "a\nb: injected")).getCode());
		InlineNode node = new InlineNode("A", "a");
		assertThrows(STXTException.class, () -> node.setValue("x\ny"));
		assertEquals("a", node.getValue());
		node.setValue("a\rb");
		assertEquals("a\rb", node.getValue());
	}

	@Test
	void textNodeRejectsLfInALineAndSplitsAStringInstead() {
		assertEquals("LINE_BREAK_NOT_ALLOWED", assertThrows(STXTException.class, () -> new TextNode("A", null, List.of("x\ny"))).getCode());
		TextNode node = new TextNode("A", "x\ny");
		assertEquals(List.of("x", "y"), node.getTextLines());
		assertThrows(STXTException.class, () -> node.addTextLine("p\nq"));
		assertThrows(STXTException.class, () -> node.setTextLines(List.of("ok", "p\nq")));
		assertEquals(List.of("x", "y"), node.getTextLines());
		node.addTextLine("z\r");
		assertEquals(List.of("x", "y", "z\r"), node.getTextLines());
	}

	// ---------------------------------------------------------------- deep trees built by a program

	@Test
	void aChainOf100000NodesIsBuiltInLinearTimeAndResolvesItsNamespace() {
		InlineNode root = new InlineNode("Root", "a.b", null);
		InlineNode node = root;
		for (int i = 0; i < 100000; i++) {
			InlineNode child = new InlineNode("N");
			node.addChild(child);
			node = child;
		}
		assertEquals(100000, node.getLevel());
		assertEquals("a.b", node.getNamespace());
		assertEquals("a.b:n", node.getQualifiedName());
	}

	@Test
	void everyCycleIsStillDetected() {
		InlineNode root = new InlineNode("Root");
		InlineNode child = new InlineNode("Child");
		InlineNode grandchild = new InlineNode("Grandchild");
		root.addChild(child);
		child.addChild(grandchild);
		assertEquals("NODE_CYCLE", assertThrows(STXTException.class, () -> root.addChild(root)).getCode());
		assertEquals("NODE_CYCLE", assertThrows(STXTException.class, () -> grandchild.addChild(root)).getCode());

		InlineNode a = new InlineNode("A");
		InlineNode b = new InlineNode("B");
		a.addChild(b);
		assertEquals("NODE_CYCLE", assertThrows(STXTException.class, () -> b.addChild(a)).getCode());
	}

	// ---------------------------------------------------------------- parser input

	@Test
	void linesAreSplitAtLfAndCrlfOnlyInEveryEntryPoint() throws IOException {
		String content = "A: one\rB: two\r\nC: three\n";
		List<String> expected = List.of("A=one\rB: two", "C=three");

		assertEquals(expected, describe(new Parser().parse(content)));
		assertEquals(expected, describe(new Parser().parseResult(content).getNodes()));
		Path file = tempDir.resolve("cr.stxt");
		Files.writeString(file, content);
		assertEquals(expected, describe(new Parser().parseFile(file.toFile())));

		List<Node> streamed = new ArrayList<>();
		Parser parser = new Parser();
		parser.registerStreamObserver(new dev.stxt.processors.StreamObserver() {
			@Override public void onRootNode(Node node) { streamed.add(node); }
			@Override public void onError(ParseException error) { throw error; }
		});
		parser.parseStream(new StringReader(content));
		assertEquals(expected, describe(streamed));

		assertEquals(List.of("A=1", "B=2"), describe(new Parser().parse("A: 1\nB: 2")));
		assertEquals(List.of(), new Parser().parse(""));
		assertEquals(List.of("T >>x\ry"), describe(new Parser().parse("T >>\n\tx\ry\n")));
	}

	private static List<String> describe(List<Node> nodes) {
		List<String> out = new ArrayList<>();
		for (Node n : nodes)
			out.add(n instanceof InlineNode i ? i.getName() + "=" + i.getValue() : n.getName() + " >>" + String.join("|", ((TextNode) n).getTextLines()));
		return out;
	}

	@Test
	void aBomIsDroppedBeforeTheLineLengthCutOnTheReaderPath() throws IOException {
		// Line 1 has exactly maxLineLength characters after the BOM: accepted whole, and the next
		// line is line 2 (the BOM used to be counted, then removed after the cut: a line one over
		// the limit passed truncated, and its tail became a line of its own).
		String line1 = "N: " + "a".repeat(9997);
		String content = "﻿" + line1 + "\nOther: y\n";
		Path file = tempDir.resolve("bom.stxt");
		Files.writeString(file, content);

		ParseResult viaFile = new Parser().parseResultFile(file.toFile());
		ParseResult viaString = new Parser().parseResult(content);
		assertEquals(0, viaFile.getErrors().size());
		assertEquals(describe(viaString.getNodes()), describe(viaFile.getNodes()));
		assertEquals(2, viaFile.getNodes().get(1).getLine());

		String over = "﻿" + line1 + "a" + "\tChild: x\nOther: y\n";
		Files.writeString(file, over);
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", new Parser().parseResultFile(file.toFile()).getErrors().get(0).getCode());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", new Parser().parseResult(over).getErrors().get(0).getCode());
	}

	@Test
	void aLimitBelowMinusOneIsRejected() {
		Parser parser = new Parser();
		assertThrows(IllegalArgumentException.class, () -> parser.setMaxNesting(-2));
		assertThrows(IllegalArgumentException.class, () -> parser.setMaxLineLength(-2));
		assertThrows(IllegalArgumentException.class, () -> parser.setMaxInputSize(-2));
		parser.setMaxNesting(-1);
		parser.setMaxLineLength(0);
		parser.setMaxInputSize(-1);
		assertEquals(0, parser.parseResult("").getErrors().size());
	}

	// ---------------------------------------------------------------- namespaces

	@Test
	void namespacesAreCheckedByALinearScan() {
		String longNs = "a.".repeat(1999) + "a";
		assertEquals(longNs, new Parser().parse("N (" + longNs + "): v\n").get(0).getNamespace());
		String huge = "a.".repeat(4990) + "a";
		assertEquals(huge, new Parser().parse("N (" + huge + "): v\n").get(0).getNamespace());
		assertEquals("INVALID_NAMESPACE", assertThrows(ParseException.class, () -> new Parser().parse("N (" + huge + "!): v\n")).getCode());

		for (String bad : List.of("a", "@", "@a", "a.", ".a", "a..b", "a b.c", "a.b!")) {
			ParseException e = assertThrows(ParseException.class, () -> new Parser().parse("N (" + bad + "): v\n"), bad);
			assertTrue(e.getCode().equals("INVALID_NAMESPACE") || e.getCode().equals("INVALID_LINE"), bad);
		}
		for (String good : List.of("a.b", "A.B", "@stxt.schema", "com.example.docs", "a1.2b"))
			assertEquals(good.toLowerCase(), new Parser().parse("N (" + good + "): v\n").get(0).getNamespace());
	}

	@Test
	void namespacesLowerCaseAsciiOnlyKelvinSignIsNotAK() {
		assertEquals("INVALID_NAMESPACE", assertThrows(ParseException.class, () -> new Parser().parse("N (Kelvin.x): v\n")).getCode());
		assertEquals("INVALID_NAMESPACE", assertThrows(ParseException.class, () -> new InlineNode("N", "Kelvin.x", "v")).getCode());
		assertEquals("com.example", StringUtils.lowerCase("Com.Example"));
		assertEquals("K", StringUtils.lowerCase("K"));
	}

	// ---------------------------------------------------------------- discovery

	/** A level whose every directory lists the same two subdirectories: a cycle of breadth 2. */
	private static final class CyclicFileSystem implements DiscoveryFileSystem {
		int listings = 0;
		@Override public boolean isDirectory(Path path) { return path.startsWith(Path.of("/p/.stxt")); }
		@Override public List<DiscoveryEntry> listDirectory(Path path) {
			listings++;
			return List.of(new DiscoveryEntry(Path.of("/p/.stxt/a"), "a", true), new DiscoveryEntry(Path.of("/p/.stxt/b"), "b", true));
		}
		@Override public String readFile(Path path) throws IOException { throw new IOException("no files"); }
	}

	@Test
	void eachDirectoryOfALevelIsVisitedOnce() {
		CyclicFileSystem fs = new CyclicFileSystem();
		DiscoveryResult result = new DiscoveryResolver(fs, NO_ENV, DiscoveryResolver.DEFAULT_MAX_ASCENT).resolve(Path.of("/p"));
		assertEquals(3, fs.listings);
		assertEquals(0, result.getErrors().size());
	}

	@Test
	void anAdapterThatThrowsDoesNotMakeResolveThrow() {
		DiscoveryFileSystem fs = new DiscoveryFileSystem() {
			@Override public boolean isDirectory(Path path) { throw new IllegalStateException("boom"); }
			@Override public List<DiscoveryEntry> listDirectory(Path path) { throw new java.io.UncheckedIOException(new IOException("boom")); }
			@Override public String readFile(Path path) { throw new java.io.UncheckedIOException(new IOException("boom")); }
		};
		DiscoveryEnvironment env = new DiscoveryEnvironment() {
			@Override public List<String> getStxtPath() { return List.of("/etc/stxt"); }
			@Override public Path getUserLevelDir() { return Path.of("/home/u/.stxt"); }
			@Override public Path getSystemLevelDir() { return Path.of("/etc/stxt"); }
		};
		DiscoveryResolver resolver = new DiscoveryResolver(fs, env, DiscoveryResolver.DEFAULT_MAX_ASCENT);
		assertEquals(List.of(), resolver.resolveChain(Path.of("/p")));
		assertEquals(0, resolver.resolve(Path.of("/p")).getErrors().size());

		// listDirectory and readFile throwing unchecked I/O errors are tolerated too
		DiscoveryFileSystem listed = new DiscoveryFileSystem() {
			@Override public boolean isDirectory(Path path) { return true; }
			@Override public List<DiscoveryEntry> listDirectory(Path path) {
				if (path.equals(Path.of("/p/.stxt"))) return List.of(new DiscoveryEntry(Path.of("/p/.stxt/x.stxt"), "x.stxt", false), new DiscoveryEntry(Path.of("/p/.stxt/sub"), "sub", true));
				throw new java.io.UncheckedIOException(new IOException("boom"));
			}
			@Override public String readFile(Path path) { throw new java.io.UncheckedIOException(new IOException("boom")); }
		};
		DiscoveryResult result = new DiscoveryResolver(listed, NO_ENV, 1).resolve(Path.of("/p"));
		assertEquals(1, result.getErrors().size());
		assertEquals(DiscoveryError.NOT_PARSEABLE, result.getErrors().get(0).getCode());
	}

	@Test
	void maxAscentMustBeNonNegative() {
		assertThrows(IllegalArgumentException.class, () -> new DiscoveryResolver(new CyclicFileSystem(), NO_ENV, -1));
	}

	@Test
	void theNioAdapterRejectsADefinitionFileAboveTheSizeBoundWithoutReadingIt() throws IOException {
		Path level = tempDir.resolve(".stxt");
		Files.createDirectories(level);
		Path big = level.resolve("big.stxt");
		try (RandomAccessFile f = new RandomAccessFile(big.toFile(), "rw")) {
			f.setLength(NioDiscoveryFileSystem.MAX_DEFINITION_FILE_BYTES + 1);	// sparse: no bytes are written
		}
		assertThrows(IOException.class, () -> new NioDiscoveryFileSystem().readFile(big));

		DiscoveryResult result = new DiscoveryResolver(new NioDiscoveryFileSystem(), NO_ENV, 1).resolve(tempDir);
		assertEquals(1, result.getErrors().size());
		assertEquals(DiscoveryError.NOT_PARSEABLE, result.getErrors().get(0).getCode());
	}
}
