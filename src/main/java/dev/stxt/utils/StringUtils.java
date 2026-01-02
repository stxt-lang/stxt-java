package dev.stxt.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {
	private StringUtils() {
	}

	// Usado para nodos name>>
	public static String rightTrim(String s) {
		if (s == null)
			return "";
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}

	// Usado para nodos tipo Base64 y Hex
	public static String cleanSpaces(String input) {
		return input.replaceAll("\\s+", "");
	}
	
	// Usado para normalizar namespace
	public static String lowerCase(String input) {
		if (input == null) return "";
		return input.toLowerCase(Locale.ROOT);
	}
	
	// Usados para name de los nodos
	public static String compactSpaces(String s) {
		if (s == null)
			return "";
		return s.trim().replaceAll("\\s+", " ");
	}

	private static final Pattern DIACRITICS = Pattern.compile("\\p{Mn}+");

	// Usados para name normalizado de nodos
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
