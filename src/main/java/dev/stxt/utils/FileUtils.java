package dev.stxt.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/** File reading helpers used by the parser. */
public class FileUtils {
	private static final String UTF8_BOM = "\uFEFF";
	
	private FileUtils() {
	}

	/**
	 * @param s string to strip the leading BOM from.
	 * @return the string without its leading UTF-8 BOM, if it had one.
	 */
	public static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM))
			s = s.substring(1);
		return s;
	}

	/**
	 * @param file file to read.
	 * @return the whole content of the file as bytes.
	 * @throws IOException if the file cannot be read or is larger than 2 GB.
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
	 * @param file file to read.
	 * @return the whole content of the file as UTF-8 text.
	 * @throws IOException if the file cannot be read.
	 */
	public static String readFileContent(File file) throws IOException {
		return new String(readFile(file), StandardCharsets.UTF_8);
	}

}