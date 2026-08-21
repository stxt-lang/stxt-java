package dev.stxt.schema.type;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Type;

/**
 * Blanks inside binary values (STXT-SCHEMA-SPEC 9.5, since 0.10.0): every space and tab is
 * removed, wherever it is and in both forms, before the grammar of {@code HEXADECIMAL},
 * {@code BINARY} and {@code BASE64} applies. Nothing else is removed.
 */
class BinaryBlanksTest {

	private static Node inline(String value) {
		return new Parser().parse("Data: " + value + "\n").get(0);
	}

	private static Node block(String... lines) {
		StringBuilder sb = new StringBuilder("Data >>\n");
		for (String line : lines)
			sb.append('\t').append(line).append('\n');
		return new Parser().parse(sb.toString()).get(0);
	}

	private static void assertInvalid(Type type, Node node) {
		ValidationException ex = assertThrows(ValidationException.class, () -> type.validate(null, node));
		assertEquals("INVALID_VALUE", ex.getCode());
	}

	@ParameterizedTest
	@ValueSource(strings = { "DEADBEEF", "DE AD BE EF", "DE\tAD", " DE  AD \t BE EF ", "D E A D" })
	void hexadecimalIgnoresEveryBlank(String value) {
		assertDoesNotThrow(() -> HEXADECIMAL.INSTANCE.validate(null, inline(value)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "DE:AD", "DE-AD", "DE_AD", "DE AD ZZ", "DE AD" })
	void hexadecimalRemovesNothingElse(String value) {
		assertInvalid(HEXADECIMAL.INSTANCE, inline(value));
	}

	@Test
	void hexadecimalEmptyAfterRemovingBlanksIsInvalid() {
		assertInvalid(HEXADECIMAL.INSTANCE, inline(""));
		assertInvalid(HEXADECIMAL.INSTANCE, block("", "\t ", ""));
	}

	@Test
	void hexadecimalBlockJoinsLinesAndDropsBlanksInside() {
		assertDoesNotThrow(() -> HEXADECIMAL.INSTANCE.validate(null, block("DE AD", "  BE\tEF  ", "", "CA FE")));
		assertInvalid(HEXADECIMAL.INSTANCE, block("DE AD", "BE:EF"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "10101010", "1010 1010", "1 0 1 0", "1010\t1010" })
	void binaryIgnoresEveryBlank(String value) {
		assertDoesNotThrow(() -> BINARY.INSTANCE.validate(null, inline(value)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "1010-1010", "1010:1010", "1010 2" })
	void binaryRemovesNothingElse(String value) {
		assertInvalid(BINARY.INSTANCE, inline(value));
	}

	@Test
	void binaryEmptyAfterRemovingBlanksIsInvalid() {
		assertInvalid(BINARY.INSTANCE, inline(" \t"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "SGVsbG8=", "SG Vs bG 8=", "SGVs bG8=", "SG\tVsbG8=", "SGVsbG8" })
	void base64IgnoresEveryBlank(String value) {
		assertDoesNotThrow(() -> BASE64.INSTANCE.validate(null, inline(value)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "SG:Vs", "SG-Vs", "SGVs_bG8=", "SGVsbG8==", "SGVsbG8=x", "S", "SGVsb G8=!" })
	void base64RejectsCharactersOutsideTheAlphabet(String value) {
		assertInvalid(BASE64.INSTANCE, inline(value));
	}

	@Test
	void base64EmptyAfterRemovingBlanksIsInvalid() {
		assertInvalid(BASE64.INSTANCE, inline(""));
		assertInvalid(BASE64.INSTANCE, inline(" "));
		assertInvalid(BASE64.INSTANCE, block(" ", ""));
	}

	@Test
	void base64BlockWrappedAt76ColumnsWithBlanksValidates() {
		// 114 bytes of zeros -> 152 Base64 characters, wrapped at 76 columns
		String encoded = java.util.Base64.getEncoder().encodeToString(new byte[114]);
		assertEquals(152, encoded.length());
		String first = "  " + encoded.substring(0, 76) + " \t";
		String second = "\t " + encoded.substring(76) + "  ";
		assertDoesNotThrow(() -> BASE64.INSTANCE.validate(null, block(first, "", second)));
		// A separator other than a blank inside the wrapped text is still invalid
		assertInvalid(BASE64.INSTANCE, block(encoded.substring(0, 76) + ",", encoded.substring(76)));
	}

	@Test
	void base64BlankInTheMiddleOfAGroupIsAllowed() {
		// "SGVsbG8gd29ybGQ=" ("Hello world") split at arbitrary places
		assertDoesNotThrow(() -> BASE64.INSTANCE.validate(null, inline("S G V s b G 8 g d 2 9 y b G Q =")));
	}
}
