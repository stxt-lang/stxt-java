package dev.stxt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceReader {
	private ResourceReader() {
	}

	public static String readResource(String resourcePath) throws IOException {
		try (InputStream is = ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null)
				throw new IllegalArgumentException("Recurso no encontrado en el classpath: " + resourcePath);

			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String metaSchemaText = ResourceReader.readResource("dev/stxt/schema/@stxt.schema.stxt");
		System.out.println(metaSchemaText);
	}
}
