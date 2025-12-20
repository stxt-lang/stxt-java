package dev.stxt.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Test;

public class Base64ReadTest {
	@Test
	public void mainTest() throws IOException {
		String text = FileUtils.readFileContent(FileTestLoction.getFile("foto.base64.txt"));
		System.out.println(text.substring(0, 200) + "...");
		byte[] fotoBytes = Base64.getDecoder().decode(StringUtils.cleanupString(text));
		System.out.println(fotoBytes.length);

		byte[] fotoBytesReal = FileUtils.readFile(FileTestLoction.getFile("foto.gif"));
		assertTrue(Arrays.equals(fotoBytes, fotoBytesReal));
	}
}
