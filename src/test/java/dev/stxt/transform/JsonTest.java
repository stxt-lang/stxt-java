package dev.stxt.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.stxt.exceptions.LimitException;
import dev.stxt.exceptions.ParseException;
import dev.stxt.exceptions.TransformException;

/** The JSON reader and writer of the transforms: RFC 8259, strict, with the limits of STXT-SPEC 11.2. */
public class JsonTest {

	private static void assertNotJson(String text) {
		TransformException e = assertThrows(TransformException.class, () -> Json.read(text), text);
		assertEquals("JSON_NOT_VALID", e.getCode(), e.toString());
	}

	private static String limitCode(String text, int maxNesting, int maxNumberLength, int maxInputSize) {
		return assertThrows(LimitException.class, () -> Json.read(text, maxNesting, maxNumberLength, maxInputSize)).getCode();
	}

	private static Map<String, Object> object(Object... members) {
		Map<String, Object> object = new LinkedHashMap<>();
		for (int i = 0; i < members.length; i += 2)
			object.put((String) members[i], members[i + 1]);
		return object;
	}

	// ----------------------------------------------------------------
	// Reading
	// ----------------------------------------------------------------

	@Test
	void readsTheLiterals() {
		assertEquals(Boolean.TRUE, Json.read("true"));
		assertEquals(Boolean.FALSE, Json.read("false"));
		assertNull(Json.read("null"));
	}

	@Test
	void readsNumbersAsBigDecimalWithTheirDigits() {
		assertEquals(new BigDecimal("40"), Json.read("40"));
		assertEquals(new BigDecimal("1.10"), Json.read("1.10"));
		assertEquals(new BigDecimal("-12.5e+3"), Json.read("-12.5e+3"));
		assertEquals(new BigDecimal("1E-2"), Json.read("1e-2"));
		assertEquals(new BigDecimal("12345678901234567890123"), Json.read("12345678901234567890123"));
	}

	@Test
	void writesTheTextOfANumberInPlainNotation() {
		assertEquals("40", Json.text(Json.read("40")));
		assertEquals("1.10", Json.text(Json.read("1.10")));
		assertEquals("100.0", Json.text(Json.read("100.0")));
		assertEquals("1000", Json.text(Json.read("1E+3")));
		assertEquals("0.012", Json.text(Json.read("1.2e-2")));
		assertEquals("0", Json.text(Json.read("-0")));
		assertEquals("true", Json.text(Json.read("true")));
		assertEquals("a", Json.text(Json.read("\"a\"")));
	}

	@Test
	void readsObjectsWithTheirKeysInOrderAndArrays() {
		Object json = Json.read("{\"b\": 1, \"a\": [true, null, \"x\", {}, []], \"c\": {\"d\": false}}");
		List<Object> array = new ArrayList<>();
		array.add(true);
		array.add(null);
		array.add("x");
		array.add(Map.of());
		array.add(List.of());
		assertEquals(object("b", new BigDecimal("1"), "a", array, "c", object("d", false)), json);
		assertEquals(List.of("b", "a", "c"), new ArrayList<>(((Map<?, ?>) json).keySet()));
	}

	@Test
	void acceptsTheFourBlanksOfJsonAndNoOther() {
		assertEquals(List.of(new BigDecimal("1")), Json.read(" \t\r\n[ \t\r\n1 \t\r\n] \t\r\n"));
		assertNotJson("\u000B1");
		assertNotJson("\u00A01");
		assertNotJson("[1,\u20282]");
	}

	@Test
	void readsTheEscapesOfAString() {
		assertEquals("\"\\/\b\f\n\r\té😀", Json.read("\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u00e9\\uD83D\\uDE00\""));
		assertEquals("ñ😀 \u2028", Json.read("\"ñ😀 \u2028\""));
		assertEquals("A", Json.read("\"\\u0041\""));
	}

	@Test
	void ignoresAByteOrderMarkAtTheStart() {
		assertEquals(new BigDecimal("1"), Json.read("\uFEFF1"));
		assertNotJson("1\uFEFF");
	}

	@Test
	void rejectsWhatRfc8259DoesNotAllow() {
		for (String text : new String[] {
				"", "  ", "{,}", "[1,]", "[,1]", "{\"a\": 1,}", "{'a': 1}", "{a: 1}", "{1: 2}", "{\"a\" 1}", "[1 2]",
				"[01]", "[-01]", "[1.]", "[.5]", "[+1]", "[-]", "[1e]", "[1e+]", "[0x10]", "NaN", "Infinity", "-Infinity",
				"tru", "nul", "True", "\"a", "\"a\nb\"", "\"\t\"", "\"\\x\"", "\"\\u12\"", "\"\\u12G4\"", "\"\\u０041\"",
				"{} []", "1 2", "/* c */ 1", "1 // c", "[1]]", "{\"a\": 1}}", "\"a\" \"b\"" })
			assertNotJson(text);
	}

	@Test
	void rejectsUnpairedSurrogates() {
		assertNotJson("\"\\uD800\"");
		assertNotJson("\"\\uDC00\"");
		assertNotJson("\"\\uD800\\u0041\"");
		assertNotJson("\"\\uD800x\"");
		assertNotJson("\"\uD800\"");
		assertNotJson("\"\uDC00\uD800\"");
	}

	@Test
	void rejectsDuplicateKeys() {
		TransformException e = assertThrows(TransformException.class, () -> Json.read("{\n\"a\": 1,\n\"a\": 2\n}"));
		assertEquals("JSON_DUPLICATE_KEY", e.getCode());
		assertEquals(3, e.getLine());
		// Keys are compared as they are: these are two keys
		assertEquals(2, ((Map<?, ?>) Json.read("{\"a\": 1, \"A\": 2}")).size());
	}

	@Test
	void anErrorSaysItsLineAndColumn() {
		TransformException e = assertThrows(TransformException.class, () -> Json.read("{\n\t\"a\": 1,\n\t\"b\": x\n}"));
		assertEquals(3, e.getLine());
		assertEquals("Unexpected character 'x' (column 7)", e.getMessage());
	}

	// ----------------------------------------------------------------
	// Limits
	// ----------------------------------------------------------------

	@Test
	void limitsTheNesting() {
		Json.read("[".repeat(100) + "]".repeat(100));
		assertEquals("LIMIT_NESTING_EXCEEDED", limitCode("[".repeat(101) + "]".repeat(101), 100, -1, -1));
		assertEquals("LIMIT_NESTING_EXCEEDED", limitCode("{\"a\": [[1]]}", 2, -1, -1));
		Json.read("{\"a\": [1]}", 2, -1, -1);
		Json.read("[".repeat(1000) + "]".repeat(1000), -1, -1, -1);
	}

	@Test
	void limitsTheSizeOfTheText() {
		Json.read("[1, 2]", -1, -1, 6);
		LimitException e = assertThrows(LimitException.class, () -> Json.read("[1, 2]", -1, -1, 5));
		assertEquals("LIMIT_INPUT_SIZE_EXCEEDED", e.getCode());
		assertEquals(ParseException.NO_LINE, e.getLine());
	}

	@Test
	void limitsTheLengthOfANumberInPlainNotation() {
		// 1e9999 is a 1 and 9999 zeros: 10000 characters, the default line length
		assertEquals(10000, Json.text(Json.read("1e9999")).length());
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", limitCode("1e10000", 100, 10000, -1));
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", limitCode("[1e999999999]", 100, 10000, -1));
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", limitCode("1e2147483648", 100, 10000, -1));
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", limitCode("1" + "0".repeat(10000), 100, 10000, -1));
		assertEquals("LIMIT_LINE_LENGTH_EXCEEDED", limitCode("-0.0000001", 100, 9, -1));
		assertEquals("-0.000001", Json.text(Json.read("-0.000001", 100, 9, -1)));
	}

	// ----------------------------------------------------------------
	// Writing
	// ----------------------------------------------------------------

	@Test
	void writesObjectsMemberByMemberAndArraysOfValuesOnOneLine() {
		Map<String, Object> json = object(
				"Name", "web-01",
				"Active", true,
				"Ports", List.of(new BigInteger("80"), new BigInteger("443")),
				"Owners", List.of(object("Person", "Ana"), object()),
				"Empty", List.of(),
				"Nested", List.of(List.of(new BigDecimal("1.50"))));
		assertEquals(String.join("\n",
				"{",
				"\t\"Name\": \"web-01\",",
				"\t\"Active\": true,",
				"\t\"Ports\": [80, 443],",
				"\t\"Owners\": [",
				"\t\t{",
				"\t\t\t\"Person\": \"Ana\"",
				"\t\t},",
				"\t\t{}",
				"\t],",
				"\t\"Empty\": [],",
				"\t\"Nested\": [",
				"\t\t[1.50]",
				"\t]",
				"}"), Json.write(json));
		assertEquals("{}", Json.write(object()));
		assertEquals("\"a\"", Json.write("a"));
	}

	@Test
	void escapesWhatJsonRequiresAndUnpairedSurrogates() {
		assertEquals("\"\\\"\\\\\\b\\f\\n\\r\\t\\u0001\\u001f/é😀\u2028\"", Json.write("\"\\\b\f\n\r\t\u0001\u001f/é😀\u2028"));
		assertEquals("\"\\ud800x\\udc00\"", Json.write("\uD800x\uDC00"));
	}

	@Test
	void readsWhatItWrites() {
		Map<String, Object> json = object("s", "a\"\n\\b\u0001😀", "n", new BigDecimal("-1.10E+5"), "l", List.of(object("x", true)));
		assertEquals(json, Json.read(Json.write(json)));
	}
}
