package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.ParseResult;
import dev.stxt.Parser;
import dev.stxt.TextNode;
import dev.stxt.exceptions.LimitException;
import dev.stxt.exceptions.ParseException;
import dev.stxt.processors.StreamObserver;

/** Parser limits (STXT-SPEC 11.2) and the StreamObserver / parseStream pair. */
public class LimitsTest {

	@TempDir
	Path tempDir;

	/** A document nesting the given number of levels (level 0 is the first), one node per level. */
	private static String nested(int levels) {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < levels; i++)
			content.append("\t".repeat(i)).append("N").append(i).append(": v\n");
		return content.toString();
	}

	/** A StreamObserver that collects everything it is notified. */
	private static class CollectingStreamObserver implements StreamObserver {
		final List<Node> roots = new ArrayList<>();
		final List<ParseException> errors = new ArrayList<>();

		@Override
		public void onRootNode(Node node) {
			roots.add(node);
		}

		@Override
		public void onError(ParseException error) {
			errors.add(error);
		}
	}

	// ------------------------------------------------------------------
	// Limits
	// ------------------------------------------------------------------

	@Test
	public void nestingDeeperThanTheDefault100LevelsAborts() {
		ParseResult result = new Parser().parseResult(nested(101));

		assertEquals(1, result.getErrors().size());
		ParseException error = result.getErrors().get(0);
		assertTrue(error instanceof LimitException);
		assertEquals("LIMIT_NESTING_EXCEEDED", error.getCode());
		assertEquals(101, error.getLine());
		// The abort leaves the open nodes unclosed: nothing is collected
		assertEquals(0, result.getNodes().size());
	}

	@Test
	public void nestingOfExactly100LevelsParsesUnderTheDefaults() {
		ParseResult result = new Parser().parseResult(nested(100));

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getNodes().size());
	}

	@Test
	public void maxNestingIsConfigurable() {
		Parser small = new Parser();
		small.setMaxNesting(3);
		assertFalse(small.parseResult(nested(3)).hasErrors());

		ParseResult result = small.parseResult(nested(4));
		assertEquals("LIMIT_NESTING_EXCEEDED", result.getErrors().get(0).getCode());
		assertEquals(4, result.getErrors().get(0).getLine());
	}

	@Test
	public void maxNestingMinusOneDisablesTheLimit() {
		Parser parser = new Parser();
		parser.setMaxNesting(-1);
		parser.setMaxInputSize(-1);
		ParseResult result = parser.parseResult(nested(150));

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getNodes().size());
	}

	@Test
	public void lineLongerThanTheDefault10000CharactersAborts() {
		ParseResult result = new Parser().parseResult("Name: " + "x".repeat(10000) + "\n");

		assertEquals(1, result.getErrors().size());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", result.getErrors().get(0).getCode());
		assertEquals(1, result.getErrors().get(0).getLine());
	}

	@Test
	public void maxLineLengthIsConfigurableAndClosedRootsStayCollected() {
		String content = "First: one\nSecond: two\nThird: " + "x".repeat(50) + "\n";
		Parser parser = new Parser();
		parser.setMaxLineLength(20);
		ParseResult result = parser.parseResult(content);

		assertEquals(1, result.getErrors().size());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", result.getErrors().get(0).getCode());
		assertEquals(3, result.getErrors().get(0).getLine());
		// First closed when line 2 was processed; Second was still open at the abort,
		// because the limit is checked before the line that would have closed it
		assertEquals(1, result.getNodes().size());
		assertEquals("First", result.getNodes().get(0).getName());
	}

	@Test
	public void inputLargerThanMaxInputSizeAborts() {
		Parser parser = new Parser();
		parser.setMaxInputSize(30);
		ParseResult result = parser.parseResult("A: 1\nB: 2\nC: 3\nD: 4\nE: 5\nF: 6\nG: 7\n");

		assertEquals(1, result.getErrors().size());
		assertEquals("LIMIT_INPUT_SIZE_EXCEEDED", result.getErrors().get(0).getCode());
	}

	@Test
	public void limitErrorAbortsMultiErrorCollectionAndIsTheLastError() {
		// A syntax error before the limit is collected; the invalid line after it is never seen
		String content = "bad line\nName: " + "x".repeat(50) + "\nalso bad\n";
		Parser parser = new Parser();
		parser.setMaxLineLength(20);
		ParseResult result = parser.parseResult(content);

		assertEquals(2, result.getErrors().size());
		assertEquals("INVALID_LINE", result.getErrors().get(0).getCode());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", result.getErrors().get(1).getCode());
	}

	@Test
	public void parseThrowsTheLimitErrorAsALimitException() {
		LimitException error = assertThrows(LimitException.class, () -> new Parser().parse(nested(101)));
		assertEquals("LIMIT_NESTING_EXCEEDED", error.getCode());
	}

	// ------------------------------------------------------------------
	// File and Reader entry points enforce the limits incrementally
	// ------------------------------------------------------------------

	@Test
	public void aFileLargerThanMaxInputSizeAbortsIncrementally() throws IOException {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 100; i++)
			content.append("Node").append(i).append(": value\n");
		Path file = tempDir.resolve("big.stxt");
		Files.writeString(file, content.toString());

		Parser parser = new Parser();
		parser.setMaxInputSize(50);	// far below the file size: the read must abort early
		ParseResult result = parser.parseResultFile(file.toFile());

		assertEquals(1, result.getErrors().size());
		assertEquals("LIMIT_INPUT_SIZE_EXCEEDED", result.getErrors().get(0).getCode());
	}

	@Test
	public void parseStreamOverAnEndlessLineWithNoBreakAbortsOnLineLength() {
		// A Reader that yields 'x' forever and never a line break: readLine() on it would loop
		// building an unbounded string; the incremental cut must abort before that.
		Reader endless = new Reader() {
			@Override
			public int read(char[] cbuf, int off, int len) {
				Arrays.fill(cbuf, off, off + len, 'x');
				return len;
			}

			@Override
			public void close() {
			}
		};

		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.setMaxLineLength(50);
		parser.setMaxInputSize(-1);
		parser.registerStreamObserver(collector);
		parser.parseStream(endless);

		assertEquals(1, collector.errors.size());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", collector.errors.get(0).getCode());
		assertEquals(0, collector.roots.size());
	}

	@Test
	public void parseFileMatchesParseForANormalFile() throws IOException {
		String content = "One: 1\nTwo: 2\n\tChild: c\n";
		Path file = tempDir.resolve("ok.stxt");
		Files.writeString(file, content);

		List<Node> fromFile = new Parser().parseFile(file.toFile());
		List<Node> fromString = new Parser().parse(content);

		assertEquals(fromString.size(), fromFile.size());
		assertEquals(2, fromFile.size());
		assertEquals("One", fromFile.get(0).getName());
		assertEquals("Two", fromFile.get(1).getName());
		assertEquals(1, ((InlineNode) fromFile.get(1)).getChildren().size());
	}

	@Test
	public void parseFileThrowsTheFirstErrorWhileParseResultFileCollectsThemAll() throws IOException {
		String content = "bad one\nbad two\n";
		Path file = tempDir.resolve("bad.stxt");
		Files.writeString(file, content);

		assertThrows(ParseException.class, () -> new Parser().parseFile(file.toFile()));

		ParseResult result = new Parser().parseResultFile(file.toFile());
		assertEquals(2, result.getErrors().size());
		assertEquals("INVALID_LINE", result.getErrors().get(0).getCode());
		assertEquals("INVALID_LINE", result.getErrors().get(1).getCode());
	}

	@Test
	public void parseFileSplitsCrLfAndCrLinesLikeTheStringPath() throws IOException {
		// \r\n and a lone \r both terminate a line, as in the whole-string path (BufferedReader).
		String content = "One: 1\r\nTwo: 2\rThree: 3\n";
		Path file = tempDir.resolve("crlf.stxt");
		Files.writeString(file, content);

		List<Node> nodes = new Parser().parseFile(file.toFile());
		assertEquals(3, nodes.size());
		assertEquals("One", nodes.get(0).getName());
		assertEquals("Two", nodes.get(1).getName());
		assertEquals("Three", nodes.get(2).getName());
	}

	// ------------------------------------------------------------------
	// StreamObserver and parseStream
	// ------------------------------------------------------------------

	@Test
	public void parseStreamHandsEachCompletedRootToOnRootNode() {
		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.registerStreamObserver(collector);
		parser.parseStream(List.of(
				"Entry: one",
				"\tDetail: a",
				"Entry: two",
				"\tDetail >>",
				"\t\ttext line"));

		assertEquals(0, collector.errors.size());
		assertEquals(2, collector.roots.size());
		assertEquals("Entry", collector.roots.get(0).getName());
		assertEquals(1, ((InlineNode) collector.roots.get(0)).getChildren().size());
		TextNode detail = (TextNode) ((InlineNode) collector.roots.get(1)).getChildren().get(0);
		assertEquals(List.of("text line"), detail.getTextLines());
	}

	@Test
	public void parseStreamReadsFromAReader() {
		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.registerStreamObserver(collector);
		parser.parseStream(new StringReader("One: 1\nTwo: 2\n"));

		assertEquals(0, collector.errors.size());
		assertEquals(2, collector.roots.size());
	}

	@Test
	public void parseStreamNotifiesErrorsByOnErrorAndKeepsGoing() {
		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.registerStreamObserver(collector);
		parser.parseStream(List.of("bad line", "Name: value"));

		assertEquals(1, collector.errors.size());
		assertEquals("INVALID_LINE", collector.errors.get(0).getCode());
		assertEquals(1, collector.roots.size());
	}

	@Test
	public void parseStreamRemovesABOMOnTheFirstLine() {
		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.registerStreamObserver(collector);
		parser.parseStream(List.of("\uFEFF" + "Name: value"));

		assertEquals(0, collector.errors.size());
		assertEquals("Name", collector.roots.get(0).getName());
	}

	@Test
	public void parseStreamStopsConsumingTheInputWhenALimitAborts() {
		int[] consumed = { 0 };
		Iterable<String> endless = () -> new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public String next() {
				consumed[0]++;
				return "Entry: " + consumed[0];
			}
		};

		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.setMaxInputSize(100);
		parser.registerStreamObserver(collector);
		parser.parseStream(endless);

		assertEquals(1, collector.errors.size());
		assertEquals("LIMIT_INPUT_SIZE_EXCEEDED", collector.errors.get(0).getCode());
		assertTrue(consumed[0] <= 12, "the endless input stopped being consumed: " + consumed[0]);
	}

	@Test
	public void streamObserverFiresInParseResultTooWithTheSameRoots() {
		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.registerStreamObserver(collector);
		ParseResult result = parser.parseResult("One: 1\nbad line\nTwo: 2\n");

		assertEquals(2, collector.roots.size());
		assertTrue(collector.roots.get(0) == result.getNodes().get(0));
		assertTrue(collector.roots.get(1) == result.getNodes().get(1));
		assertEquals(1, collector.errors.size());
		assertTrue(collector.errors.get(0) == result.getErrors().get(0));
	}

	@Test
	public void inFailFastParseTheStreamObserverSeesTheThrownError() {
		// parse() stops at the first error (a documented difference with the TypeScript port,
		// which walks the whole document): the observer sees exactly that error before the throw
		CollectingStreamObserver collector = new CollectingStreamObserver();
		Parser parser = new Parser();
		parser.registerStreamObserver(collector);

		ParseException thrown = assertThrows(ParseException.class, () -> parser.parse("bad one\nbad two\n"));
		assertEquals(1, collector.errors.size());
		assertTrue(collector.errors.get(0) == thrown);
	}
}
