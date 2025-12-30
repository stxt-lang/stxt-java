package dev.stxt.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {
	private StringUtils() {
	}

	public static String rightTrim(String s) {
		if (s == null)
			return null;
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}

	public static String cleanSpaces(String input) {
		return input.replaceAll("\\s+", "");
	}
	
	public static String lowerCase(String input) {
		if (input == null) return "";
		return input.toLowerCase(Locale.ROOT);
	}
	
	public static String compactSpaces(String s) {
		if (s == null)
			return null;
		return s.trim().replaceAll("\\s+", " ");
	}

	private static final Pattern DIACRITICS = Pattern.compile("\\p{Mn}+");

	public static String normalize(String input) {
	    if (input == null) return "";
	    String s = input.trim();
	    if (s.isEmpty()) return "";

	    s = Normalizer.normalize(s, Normalizer.Form.NFKD);
	    s = DIACRITICS.matcher(s).replaceAll("");
	    s = s.toLowerCase(Locale.ROOT);
	    s = compactSpaces(s);
	    
	    // cualquier cosa que no sea [a-z0-9] => '-'
	    s = s.replaceAll("[^a-z0-9]+", "-");
	    // trim de '-'
	    s = s.replaceAll("^-+|-+$", "");
	    return s;
	}
	
}
