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
	
	private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+"); // Marks (acentos, diéresis, etc.)

    private static String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("");
    }	
    
	private static String compactSpaces(String s) {
		if (s == null)
			return null;
		return s.trim().replaceAll("\\s+", " ");
	}

    public static String normalizeName(String name)
    {
    	if (name == null) return "";
    	return compactSpaces(removeDiacritics(name.trim().toLowerCase(Locale.ROOT)));
    }
}
