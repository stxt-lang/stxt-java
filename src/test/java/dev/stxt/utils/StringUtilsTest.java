package dev.stxt.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {
	@Test
	void demo() {
		checkEquals("Hola Mundo", "hola-mundo");
		checkEquals("   Hola   Mundo ", "hola-mundo");
		checkEquals("   Hola${}:   Mundo ", "hola-mundo");
	}

	private void checkEquals(String normal, String normalized) {
		Assertions.assertEquals(StringUtils.normalizeFull(normal), normalized);
		
	}
}
