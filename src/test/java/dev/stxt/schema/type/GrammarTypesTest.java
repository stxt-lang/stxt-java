package dev.stxt.schema.type;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Type;

/** The grammar of the value types of STXT-SCHEMA-SPEC 9.4 is normative: calendar and clock ranges included. */
class GrammarTypesTest {

	private record Case(Type type, List<String> good, List<String> bad) {
	}

	private static final List<Case> CASES = List.of(
			new Case(NUMBER.INSTANCE,
					List.of("1", "-1.5", "+1", "1.", ".5", "007", "1e10", "1.2E-3"),
					List.of("abc", "1,5", "", "1e", "e5", "1.2.3")),
			new Case(DATE.INSTANCE,
					List.of("2026-08-21", "2024-02-29", "0000-01-01", "9999-12-31"),
					List.of("2026-02-30", "2026-13-01", "2026-00-10", "2026-04-31", "2023-02-29", "2026-8-21", "21-08-2026", "2026-08-21T10:00")),
			new Case(TIME.INSTANCE,
					List.of("00:00:00", "23:59:59"),
					List.of("24:00:00", "10:60:00", "10:00:60", "10:30", "1:30:00", "10:30:00.5", "10:30:00Z")),
			new Case(TIMESTAMP.INSTANCE,
					List.of("2026-08-21T10:30", "2026-08-21T10:30:00", "2026-08-21T10:30:00.1", "2026-08-21T10:30:00.123456Z",
							"2026-08-21T10:30:00+02:00", "2024-02-29T23:59:59-23:59"),
					List.of("2026-02-30T10:30", "2026-08-21T24:00", "2026-08-21T10:60:00", "2026-08-21T10:30:00+24:00",
							"2026-08-21T10:30:00+02:60", "2026-08-21 10:30:00", "2026-08-21", "2026-08-21T10:30:00.", "2026-08-21T10:30:00+0200")));

	private static Node inline(String value) {
		return new Parser().parse("Value: " + value + "\n").get(0);
	}

	@TestFactory
	Stream<DynamicTest> grammar() {
		return CASES.stream().flatMap(c -> Stream.concat(
				c.good().stream().map(v -> DynamicTest.dynamicTest(c.type().getName() + " accepts \"" + v + "\"",
						() -> assertDoesNotThrow(() -> c.type().validate(null, inline(v))))),
				Stream.concat(
						c.bad().stream().map(v -> DynamicTest.dynamicTest(c.type().getName() + " rejects \"" + v + "\"", () -> {
							ValidationException ex = assertThrows(ValidationException.class, () -> c.type().validate(null, inline(v)));
							assertEquals("INVALID_VALUE", ex.getCode());
						})),
						Stream.of(DynamicTest.dynamicTest(c.type().getName() + " rejects the block form", () -> {
							Node node = new Parser().parse("Value >>\n\t" + c.good().get(0) + "\n").get(0);
							ValidationException ex = assertThrows(ValidationException.class, () -> c.type().validate(null, node));
							assertEquals("NOT_ALLOWED_TEXT", ex.getCode());
						})))));
	}
}
