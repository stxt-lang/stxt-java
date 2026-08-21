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

/** The {@code URL} value type (STXT-SCHEMA-SPEC 9.4): absolute URL, scheme and host mandatory, own grammar. */
class URLTest {

	private static Node inline(String value) {
		return new Parser().parse("Url: " + value + "\n").get(0);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"https://stxt.dev",
			"https://stxt.dev/path/to?q=1&r=2#frag",
			"HTTP://EXAMPLE.COM/",
			"http://localhost:8080/",
			"ftp://user:pw@example.com/dir/",
			"http://[::1]:80/x",
			"http://192.168.0.1",
			"git+ssh://host/repo.git",
			"https://例え.jp/パス",
			"http://host?q=1",
	})
	void acceptsAbsoluteUrls(String value) {
		assertDoesNotThrow(() -> URL.INSTANCE.validate(null, inline(value)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"stxt.dev",
			"www.stxt.dev/x",
			"mailto:ana@example.com",
			"urn:isbn:9780131103627",
			"tel:+34600000000",
			"file:///etc/hosts",
			"http://",
			"://stxt.dev",
			"http:/stxt.dev",
			"1http://stxt.dev",
			"https://exa mple.com",
			"https://host:abc",
			"http://[::1",
			"http://user@",
			"https://host/path with space",
			"",
	})
	void rejectsEverythingElse(String value) {
		ValidationException ex = assertThrows(ValidationException.class, () -> URL.INSTANCE.validate(null, inline(value)));
		assertEquals("INVALID_VALUE", ex.getCode());
	}

	@Test
	void rejectsTheBlockForm() {
		Node node = new Parser().parse("Url >>\n\thttps://stxt.dev\n").get(0);
		ValidationException ex = assertThrows(ValidationException.class, () -> URL.INSTANCE.validate(null, node));
		assertEquals("BLOCK_FORM_NOT_ALLOWED", ex.getCode());
	}
}
