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

/** The {@code EMAIL} value type (STXT-SCHEMA-SPEC 9.4): bare address, or display name plus {@code <address>}. */
class EMAILTest {

	private static Node inline(String value) {
		return new Parser().parse("Email: " + value + "\n").get(0);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"ana@example.com",
			"a.b+c@sub.example.org",
			// STXT-SCHEMA-SPEC 9.4: display name followed by the address between angle brackets
			"Ana García <ana@example.com>",
			"Ana<ana@example.com>",
			"Ana García   <ana@example.com>",
			"\"García, Ana\" <ana@example.com>",
	})
	void acceptsValidAddresses(String value) {
		assertDoesNotThrow(() -> EMAIL.INSTANCE.validate(null, inline(value)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"ana@",
			"@example.com",
			"ana@localhost",
			"a b@example.com",
			// the bracketed form needs a name, balanced brackets, a valid address and nothing after
			"<ana@example.com>",
			"   <ana@example.com>",
			"Ana <ana@>",
			"Ana <ana@localhost>",
			"Ana <ana@example.com",
			"Ana ana@example.com>",
			"Ana ana@example.com",
			"Ana <ana@example.com> extra",
			"Ana <ana@example.com> <ana@example.com>",
			"Ana <<ana@example.com>>",
	})
	void rejectsInvalidAddresses(String value) {
		ValidationException ex = assertThrows(ValidationException.class, () -> EMAIL.INSTANCE.validate(null, inline(value)));
		assertEquals("INVALID_VALUE", ex.getCode());
	}

	@Test
	void rejectsTheBlockForm() {
		Node node = new Parser().parse("Email >>\n\tana@example.com\n").get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> EMAIL.INSTANCE.validate(null, node));
		assertEquals("BLOCK_FORM_NOT_ALLOWED", ex.getCode());
	}
}
