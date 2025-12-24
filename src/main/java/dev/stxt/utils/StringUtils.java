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
	
	private static final Pattern DIACRITICS = Pattern.compile("\\p{Mn}+");

	private static String compactSpaces(String s) {
		if (s == null)
			return null;
		return s.trim().replaceAll("\\s+", " ");
	}

	public static String normalizeName(String name) {
	    if (name == null) return "";
	    String s = name.trim();
	    s = Normalizer.normalize(s, Normalizer.Form.NFKD);
	    s = DIACRITICS.matcher(s).replaceAll("");
	    s = s.toLowerCase(Locale.ROOT);
	    return compactSpaces(s);
	}
	
    /**
     * Normalize a node name into a stable ASCII "slug".
     *
     * Output:
     * - only [a-z0-9-]
     * - words separated by single '-'
     * - trimmed (no leading/trailing '-')
     *
     * Pipeline:
     * 1) trim
     * 2) NFKD
     * 3) remove combining marks (Mn)
     * 4) lowercase (Locale.ROOT)
     * 5) map non-alnum to '-'
     * 6) collapse '-' and trim
     */
    public static String normalizeNodeName(String input) {
        if (input == null) return "";

        String s = input.trim();
        if (s.isEmpty()) return "";

        // 1) Unicode compatibility decomposition
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);

        // 2) Build slug
        StringBuilder out = new StringBuilder(s.length());
        boolean lastWasDash = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            // Remove combining marks (accents, diaeresis, etc.)
            int type = Character.getType(ch);
            if (type == Character.NON_SPACING_MARK) { // Mn
                continue;
            }

            // Lowercase in a locale-stable way
            ch = Character.toLowerCase(ch);

            // Keep ASCII a-z / 0-9; everything else becomes a separator '-'
            boolean isAsciiLower = (ch >= 'a' && ch <= 'z');
            boolean isDigit = (ch >= '0' && ch <= '9');

            if (isAsciiLower || isDigit) {
                out.append(ch);
                lastWasDash = false;
            } else {
                // treat any non-alnum as separator '-'
                if (!lastWasDash && out.length() > 0) {
                    out.append('-');
                    lastWasDash = true;
                }
            }
        }

        // 3) Trim trailing '-'
        int len = out.length();
        while (len > 0 && out.charAt(len - 1) == '-') {
            out.setLength(len - 1);
            len--;
        }

        return out.toString();
    }	
}
