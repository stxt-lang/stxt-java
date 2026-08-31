package dev.stxt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** File reading helpers used by the parser. */
public class FileUtils {
	private static final String UTF8_BOM = "\uFEFF";
	
	private FileUtils() {
	}

	/**
	 * Removes the leading UTF-8 BOM of a string.
	 *
	 * @param s string to strip the leading BOM from.
	 * @return the string without its leading UTF-8 BOM, if it had one.
	 */
	public static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM))
			s = s.substring(1);
		return s;
	}

	/**
	 * Reads a whole file as bytes.
	 *
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
	 * Reads a whole file as UTF-8 text. The decode is strict (STXT-SPEC 3): a file that is
	 * not valid UTF-8 is rejected with an {@link IOException} — an I/O-level error, like a
	 * missing file — never decoded by silently substituting the invalid sequences with
	 * U+FFFD, which would make two tools see different documents from the same bytes.
	 *
	 * @param file file to read.
	 * @return the whole content of the file as UTF-8 text.
	 * @throws IOException if the file cannot be read or is not valid UTF-8.
	 */
	public static String readFileContent(File file) throws IOException {
		// CharacterCodingException is an IOException
		return strictUTF8Decoder().decode(ByteBuffer.wrap(readFile(file))).toString();
	}

	/**
	 * Opens a buffered UTF-8 reader over a file, for streaming line-by-line consumption so
	 * that the parser can apply its size and line-length limits incrementally instead of
	 * holding the whole file in memory (STXT-SPEC 11.2). The caller closes it. The decode is
	 * strict, as in {@link #readFileContent(File)}: invalid UTF-8 surfaces as an
	 * {@link IOException} while reading.
	 *
	 * @param file file to read.
	 * @return a buffered {@link Reader} decoding the file as UTF-8.
	 * @throws IOException if the file cannot be opened.
	 */
	public static Reader newFileReader(File file) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), strictUTF8Decoder()));
	}

	private static CharsetDecoder strictUTF8Decoder() {
		return StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
	}

}