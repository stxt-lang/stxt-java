package dev.stxt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LineIndentTest {

	// ------------------------
	// Helpers para los tests
	// ------------------------

	private ParseState newEmptyState() {
		return new ParseState();
	}

	private ParseState newStateWithMultilineTextNode() {
		ParseState state = new ParseState();
		Node textNode = new Node(1, 0, "Content", null, true, "");
		state.getStack().push(textNode);
		return state;
	}

	// ------------------------
	// Tests fuera de multilínea
	// ------------------------

	@Test
	void emptyLine_outsideMultiline_isIgnored() throws ParseException {
		ParseState state = newEmptyState();

		LineIndent li = LineIndent.parseLine("   ", 1, state);

		assertNull(li);
	}

	@Test
	void commentLine_outsideMultiline_isIgnored() throws ParseException {
		ParseState state = newEmptyState();

		LineIndent rs = LineIndent.parseLine("   # This is a comment", 1, state);
		assertNull(rs, "Debe ser null");
	}

	@Test
	void indentationWithSpaces_multipleOfFour_isValid() throws ParseException {
		ParseState state = newEmptyState();

		LineIndent li = LineIndent.parseLine("    Title", 1, state); // 4 espacios

		assertNotNull(li);
		assertEquals(1, li.indentLevel);
		assertEquals("Title", li.lineWithoutIndent);
	}

	@Test
	void indentationWithTab_isValid() throws ParseException {
		ParseState state = newEmptyState();

		LineIndent li = LineIndent.parseLine("\tTitle", 1, state); // 1 tab

		assertNotNull(li);
		assertEquals(1, li.indentLevel);
		assertEquals("Title", li.lineWithoutIndent);
	}

	// ------------------------
	// Tests en multilínea (TEXT)
	// ------------------------

	@Test
	void insideMultiline_hashIsNotComment_whenIndentAtOrBeyondStackLevel() throws ParseException {
		ParseState state = newStateWithMultilineTextNode();
		// stackSize = 1, multiline = true
		// 4 espacios => level 1 == stackSize → dentro del bloque TEXT

		LineIndent li = LineIndent.parseLine("    # not a comment", 2, state);

		assertNotNull(li);
		// nivel 1, mismo que el nodo TEXT
		assertEquals(1, li.indentLevel);
		// se aplica rightTrim sobre el contenido del bloque
		assertEquals("# not a comment", li.lineWithoutIndent);
	}

	@Test
	void multiline_indentedLine_beyondStackLevel_isReturnedAsText() throws ParseException {
		ParseState state = newStateWithMultilineTextNode();
		// stackSize = 1, multiline = true
		// 8 espacios => level llega a 1 (stackSize) y se corta ahí
		// Dentro del bloque, se conserva la indentación adicional como texto

		LineIndent li = LineIndent.parseLine("        line", 3, state);

		assertNotNull(li);
		// Se devuelve el nivel del nodo multilínea (no se consumen más niveles
		// estructurales)
		assertEquals(1, li.indentLevel);
		// rightTrim solo recorta por la derecha; los 4 espacios extra tras la
		// indentación base se conservan
		assertEquals("    line", li.lineWithoutIndent);
	}

	@Test
	void multiline_emptyLine_isPreservedAsTextWithStackIndentLevel() throws ParseException {
		ParseState state = newStateWithMultilineTextNode();
		// stackSize = 1, multiline = true
		// línea vacía → se preserva como texto, con indentLevel = stackSize

		LineIndent li = LineIndent.parseLine("   \t  \t  ", 4, state);

		assertNotNull(li);
		assertEquals(1, li.indentLevel);
		assertEquals("", li.lineWithoutIndent);
	}

	@Test
	void multiline_commentLineAtLowerIndent_isIgnored() throws ParseException {
		ParseState state = newStateWithMultilineTextNode();
		// stackSize = 1, multiline = true
		// Comentario con indentación < stackSize → se trata como comentario general y
		// se ignora

		LineIndent li = LineIndent.parseLine("# outside text", 5, state);

		assertNull(li);
	}
}