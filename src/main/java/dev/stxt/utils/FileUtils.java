package dev.stxt.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/** Utilidades de lectura de ficheros usadas por el parser. */
public class FileUtils {
	private static final String UTF8_BOM = "\uFEFF";
	
	private FileUtils() {
	}

	/**
	 * @param s cadena de la que eliminar el BOM inicial.
	 * @return la cadena sin el BOM UTF-8 inicial, si lo tenía.
	 */
	public static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM))
			s = s.substring(1);
		return s;
	}

	/**
	 * @param file fichero a leer.
	 * @return el contenido completo del fichero como bytes.
	 * @throws IOException si el fichero no se puede leer o supera los 2 GB.
	 */
	public static byte[] readFile(File file) throws IOException {
		try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
			// Get and check length
			long longlength = f.length();
			int length = (int) longlength;
			if (length != longlength)
				throw new IOException("File size >= 2 GB");

			// Read file and return data
			byte[] data = new byte[length];
			f.readFully(data);
			return data;
		}
	}

	/**
	 * @param file fichero a leer.
	 * @return el contenido completo del fichero como texto UTF-8.
	 * @throws IOException si el fichero no se puede leer.
	 */
	public static String readFileContent(File file) throws IOException {
		return new String(readFile(file), StandardCharsets.UTF_8);
	}

}