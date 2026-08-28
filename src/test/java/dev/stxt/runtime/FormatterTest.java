package dev.stxt.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.stxt.Parser;
import dev.stxt.runtime.NodeWriter.IndentStyle;

/** The reformatting of STXT-TREE-SPEC 12; a replica of the TypeScript suite. */
public class FormatterTest {

	private static final IndentStyle TABS = IndentStyle.TABS;
	private static final IndentStyle SPACES_4 = IndentStyle.SPACES_4;

	private static String format(String text) {
		return Formatter.format(text, TABS).text();
	}

	private static String format(String text, IndentStyle style) {
		return Formatter.format(text, style).text();
	}

	private static String canonical(String text) {
		return TreeJson.toCanonicalJson(new Parser().parse(text));
	}

	private static final String MESSY = String.join("\n",
		"# top comment", "Documento (test.fmt):   ", "    # indented comment", "    Titulo:Hello   ", "",
		"\tCuerpo >>", "\t\tfirst line", "", "\t\t    indented content", "\t\t\t\t", "\tAfter (test.fmt): block", "");

	private static final String MESSY_TABS = String.join("\n",
		"# top comment", "Documento (test.fmt):", "\t# indented comment", "\tTitulo: Hello", "",
		"\tCuerpo >>", "\t\tfirst line", "\t\t", "\t\t    indented content", "", "\tAfter (test.fmt): block", "");

	private static final String MESSY_SPACES = String.join("\n",
		"# top comment", "Documento (test.fmt):", "    # indented comment", "    Titulo: Hello", "",
		"    Cuerpo >>", "        first line", "        ", "            indented content", "", "    After (test.fmt): block", "");

	@Test
	void rewritesTheIndentationAccordingToTheLevelOfTheNode() {
		assertEquals("Padre: p\n\tHijo: v", format("Padre: p\n    Hijo: v"));
		assertEquals("Padre: p\n    Hijo: v", format("Padre: p\n\tHijo: v", SPACES_4));
	}

	@Test
	void writesExactlyOneSpaceAfterTheColonAndNoneWithoutAValue() {
		assertEquals("Doc: hola", format("Doc:    hola   "));
		assertEquals("Doc: hola", format("Doc:hola"));
		assertEquals("Contenedor:", format("Contenedor:"));
		assertEquals("Contenedor:", format("Contenedor:   "));
		assertEquals("Contenedor (ns.uno):", format("Contenedor (ns.uno):"));
	}

	@Test
	void writesTheNamespaceOnlyWhereTheSourceWroteIt() {
		String text = "Doc (a.b): x\n\tHijo (a.b): y\n\tOtro: z\n\tBloque (c.d) >>\n\t\ttexto";
		assertEquals(text, format(text));
		assertEquals("Doc (a.b): x\n\tHijo (a.b): y", format("Doc (A.B):x\n\tHijo   (a.b):y"));
	}

	@Test
	void rendersABlockLineAsNameOneSpaceAndArrows() {
		assertEquals("Doc >>\n\tuna", format("Doc  >>\n\tuna"));
		assertEquals("Doc >>\n\tuna", format("Doc>>   \n\tuna"));
	}

	@Test
	void keepsTheNameAsParsedBlanksCollapsed() {
		assertEquals("Mi Nodo: v", format("Mi   Nodo  : v"));
	}

	@Test
	void reindentsBlockLinesKeepingTheirOwnExtraIndentation() {
		assertEquals("Doc >>\n\tuna línea\n\t    sangrada", format("Doc >>\n    una línea\n        sangrada"));
		assertEquals("Doc >>\n    una línea\n    \tsangrada", format("Doc >>\n\tuna línea\n\t\tsangrada", SPACES_4));
	}

	@Test
	void indentsTheBlankLinesOfABlockBeforeMoreTextFinalOnesStayPlain() {
		// STXT-SPEC 10.3: a blank line before more block text gets the block's indentation;
		// the final blank lines of a block are not content and stay plain.
		assertEquals("Doc >>\n\tuna\n\t\n\t\n\totra", format("Doc >>\n\tuna\n\n\t\t\t\n\totra"));
		assertEquals("Doc >>\n    una\n    \n    otra", format("Doc >>\n\tuna\n\n\totra", SPACES_4));
		assertEquals("Doc >>\n\tuna\n", format("Doc >>\n\tuna\n\t\t\t"));
		assertEquals("Doc >>\n\tuna\n\nOtro: x", format("Doc >>\n\tuna\n\t\t\t\nOtro: x"));
		assertEquals("Padre:\n\tHijo: v\n\n\tOtro: w", format("Padre:\n\tHijo: v\n\t\n\tOtro: w"));
	}

	@Test
	void keepsTheTextOfTheBlockByteIdentical() {
		String text = "Doc >>\n\tuna\n\n\t\t  dos\n\t\t\t";
		assertEquals("una\n\n\t  dos", new Parser().parse(text).get(0).getText());
		assertEquals(new Parser().parse(text).get(0).getText(), new Parser().parse(format(text)).get(0).getText());
		assertEquals(new Parser().parse(text).get(0).getText(), new Parser().parse(format(text, SPACES_4)).get(0).getText());
	}

	@Test
	void removesTheTrailingBlanksOfATextLine() {
		assertEquals("Doc >>\n\tuna\n\tdos", format("Doc >>\n\tuna   \n\tdos\t"));
	}

	private static final String COMMENTS = String.join("\n",
		"# top comment", "Documento (test.fmt):", "\t# tab comment", "    # spaces comment", "\tTitulo: Hello",
		"\t\t# two units, after a childless node   ", "");

	@Test
	void convertsTheIndentationUnitsOfEveryComment() {
		assertEquals(String.join("\n", "# top comment", "Documento (test.fmt):", "\t# tab comment", "\t# spaces comment",
			"\tTitulo: Hello", "\t\t# two units, after a childless node", ""), format(COMMENTS));
		assertEquals(String.join("\n", "# top comment", "Documento (test.fmt):", "    # tab comment", "    # spaces comment",
			"    Titulo: Hello", "        # two units, after a childless node", ""), format(COMMENTS, SPACES_4));
		assertEquals("#  a   b\t c", format("#  a   b\t c"));
	}

	@Test
	void keepsEverythingAroundTheNodeLines() {
		assertEquals(MESSY_TABS, format(MESSY));
		assertEquals(MESSY_SPACES, format(MESSY, SPACES_4));
	}

	@Test
	void isIdempotentAndRoundTripsBetweenTheTwoStyles() {
		assertEquals(MESSY_TABS, format(MESSY_TABS));
		assertEquals(MESSY_SPACES, format(MESSY_SPACES, SPACES_4));
		assertEquals(MESSY_TABS, format(MESSY_SPACES));
		assertEquals(MESSY_SPACES, format(MESSY_TABS, SPACES_4));
	}

	@Test
	void keepsTheLineEndingThePresenceOfAFinalNewlineAndRemovesABom() {
		assertEquals("Doc:\r\n\tHijo: v\r\n", format("Doc:\r\n    Hijo: v\r\n"));
		assertEquals("Doc:\n\tHijo: v", format("Doc:\n    Hijo: v"));
		assertEquals("Doc:\n\tHijo: v\n", format("Doc:\n    Hijo: v\n"));
		assertEquals("", format(""));
		assertEquals("Doc: x\n", format("﻿Doc: x\n"));
		assertEquals("# comment\nDoc: x\n", format("﻿# comment\nDoc: x\n"));
	}

	@Test
	void producesTheSameCanonicalTreeAndNoErrors() {
		assertEquals(canonical(MESSY), canonical(format(MESSY)));
		assertEquals(canonical(MESSY), canonical(format(MESSY, SPACES_4)));
		assertTrue(Formatter.format(MESSY).errors().isEmpty());
	}

	@Test
	void reportsTheErrorsAndConvertsOnlyTheUnitsOfTheLinesTheTreeDoesNotDescribe() {
		FormatResult result = Formatter.format("Doc: x\n\t  Mixed: y\n\t\t\tJump: z\n", SPACES_4);
		assertEquals(List.of("2:INDENTATION_MIXED", "3:INDENTATION_LEVEL_NOT_VALID"),
			result.errors().stream().map(e -> e.getLine() + ":" + e.getCode()).toList());
		assertEquals("Doc: x\n      Mixed: y\n            Jump: z\n", result.text());

		String text = "Padre: p\n\t\t\tHijo: v";
		assertEquals(text, format(text));

		FormatResult still = Formatter.format("Doc:   x\n    Hijo:y\n\t\t\t\tJump: z");
		assertEquals(1, still.errors().size());
		assertEquals("Doc: x\n\tHijo: y\n\t\t\t\tJump: z", still.text());
	}

	@Test
	public void parserLimitsReachTheInternalParser() {
		String longLine = "Name: " + "x".repeat(10000) + "\n";

		// Default limits apply
		FormatResult limited = Formatter.format(longLine, TABS);
		assertEquals(1, limited.errors().size());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", limited.errors().get(0).getCode());

		// -1 disables the limit and the long line formats
		FormatResult unlimited = Formatter.format(longLine, TABS, 100, -1, 10000000);
		assertEquals(0, unlimited.errors().size());
		assertEquals(longLine, unlimited.text());

		// After an abort, undescribed lines are unit-converted only
		FormatResult aborted = Formatter.format("A: 1\n    B: " + "y".repeat(30) + "\n    C: 3\n", TABS, 100, 20, 10000000);
		assertEquals(1, aborted.errors().size());
		assertEquals("A: 1\n\tB: " + "y".repeat(30) + "\n\tC: 3\n", aborted.text());
	}
}
