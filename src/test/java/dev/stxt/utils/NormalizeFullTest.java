package dev.stxt.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Canonical name as per STXT-SPEC 4.3 (IDN model): NFC + Unicode lower case,
// separators ('-', '_', space) collapse into a single '-', and diacritics
// and non-Latin alphabets are kept.
public class NormalizeFullTest {
	@Test
	void demo() {
		checkEquals("Hola Mundo", "hola-mundo");
		checkEquals("   Hola   Mundo ", "hola-mundo");
		checkEquals("   Hola_ Mundo ", "hola-mundo");
		checkEquals("   Hola_Mundo ", "hola-mundo");
		checkEquals("   Hola-_- Mundo ", "hola-mundo");
		checkEquals("-Hola Mundo-", "hola-mundo");
	}

	@Test
	void diacriticsAndAlphabetsArePreserved() {
		checkEquals("Título", "título");                 // Título => título (accents are kept)
		checkEquals("   Hólä_Mundo ", "hólä-mundo");
		checkEquals("Пример 1", "пример-1"); // Пример 1 => пример-1

		// Accent sensitive: 'Año' and 'Ano' are different nodes (spec 4.3)
		Assertions.assertNotEquals(StringUtils.normalize("Año"), StringUtils.normalize("Ano"));
	}

	@Test
	void unicodeNFCNormalization() {
		// precomposed 'é' (U+00E9) and 'e' + combining accent (U+0301) are the same name
		String precomposed = "Caf\u00E9";
		String decomposed = "Cafe\u0301";
		checkEquals(precomposed, "café");
		checkEquals(decomposed, "café");
		Assertions.assertEquals(StringUtils.normalize(precomposed), StringUtils.normalize(decomposed));
	}

	@Test
	void emptyCanonicalName() {
		// Separators only => empty canonical name (invalid at Node level, spec 4.2/4.3)
		checkEquals("___", "");
		checkEquals(" - _ ", "");
	}

	private void checkEquals(String normal, String normalized) {
		Assertions.assertEquals(normalized, StringUtils.normalize(normal));
	}
}
