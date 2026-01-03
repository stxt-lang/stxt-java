package dev.stxt.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NormalizeFullTest {
	@Test
	void demo() {
		checkEquals("Hola Mundo", "hola-mundo");
		checkEquals("   Hola   Mundo ", "hola-mundo");
		checkEquals("   Hola${}:   Mundo ", "hola-mundo");
		checkEquals("   Hola_ Mundo ", "hola-mundo");
		checkEquals("   Hola_Mundo ", "hola-mundo");
		checkEquals("   Hólä_Mundo ", "hola-mundo");
		checkEquals("   Hólä__$$Mundo ", "hola-mundo");
	}

	private void checkEquals(String normal, String normalized) {
		System.out.println(normal + " => " + StringUtils.normalize(normal));
		Assertions.assertEquals(normalized, StringUtils.normalize(normal));
		
	}
}
