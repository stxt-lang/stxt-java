package dev.stxt.utils;

public class StringUtils {
	private StringUtils() {
	}

	// Trim derecha
	public static String rightTrim(String s) {
		if (s == null)
			return null;
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}

	// Trim a ambos lados y compacta espacios/tabs internos a uno
	public static String compactString(String s) {
		if (s == null)
			return null;
		// \s+ coincide con uno o más espacios en blanco (incluye tab, etc.)
		return s.trim().replaceAll("\\s+", " ");
	}

	public static String cleanupString(String input) {
		return input.replaceAll("\\s+", "");
	}

}
