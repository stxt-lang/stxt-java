package dev.stxt.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class FileUtils {
	private static final String UTF8_BOM = "\uFEFF";
	
	private FileUtils() {
	}

	public static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM))
			s = s.substring(1);
		return s;
	}

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

	public static String readFileContent(File file) throws IOException {
		return new String(readFile(file), StandardCharsets.UTF_8);
	}

}