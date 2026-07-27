package dev.stxt.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Nombre canónico según STXT-SPEC 4.3 (modelo IDN): NFC + minúsculas Unicode,
// separadores ('-', '_', espacio) colapsan a un solo '-', y los diacríticos
// y alfabetos no latinos se conservan.
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
	void diacriticosYAlfabetosSeConservan() {
		checkEquals("Título", "título");                 // Título => título
		checkEquals("   Hólä_Mundo ", "hólä-mundo");
		checkEquals("Пример 1", "пример-1"); // Пример 1 => пример-1

		// Sensible a acentos: 'Año' y 'Ano' son nodos distintos (spec 4.3)
		Assertions.assertNotEquals(StringUtils.normalize("Año"), StringUtils.normalize("Ano"));
	}

	@Test
	void normalizacionUnicodeNFC() {
		// 'é' precompuesto (U+00E9) y 'e' + acento combinante (U+0301) son el mismo nombre
		String precompuesto = "Caf\u00E9";
		String descompuesto = "Cafe\u0301";
		checkEquals(precompuesto, "café");
		checkEquals(descompuesto, "café");
		Assertions.assertEquals(StringUtils.normalize(precompuesto), StringUtils.normalize(descompuesto));
	}

	@Test
	void nombreCanonicoVacio() {
		// Solo separadores => nombre canónico vacío (inválido a nivel de Node, spec 4.2/4.3)
		checkEquals("___", "");
		checkEquals(" - _ ", "");
	}

	private void checkEquals(String normal, String normalized) {
		Assertions.assertEquals(normalized, StringUtils.normalize(normal));
	}
}
