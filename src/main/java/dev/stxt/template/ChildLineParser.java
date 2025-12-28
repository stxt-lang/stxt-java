package dev.stxt.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.ParseException;

public final class ChildLineParser {

    private static final Pattern CHILD_LINE_PATTERN = Pattern.compile(
            "^\\s*" +
            "(?:\\(\\s*(?<count>[^()\\s][^)]*?)\\s*\\)\\s*)?" + // (count)
            "(?<type>[^()]*)" +                                // type (puede estar vacío)
            "\\s*$"
        );
    
    private ChildLineParser() {
        // utility
    }

    /**
     * Parsea una línea de hijo del tipo:
     *   (count) name (namespace)
     * donde \`(count)\` y \`(namespace)\` son opcionales.
     *
     * @param rawLine     Línea tal cual del fichero.
     * @param inheritedNs Namespace heredado (puede ser \`null\`).
     * @param lineNumber  Número de línea para errores.
     */
    public static ChildLine parse(
            String rawLine,
            int lineNumber
    ) {
    	if (rawLine.trim().isEmpty())
    		return new ChildLine(null, null, lineNumber, rawLine);
    	
        Matcher m = CHILD_LINE_PATTERN.matcher(rawLine);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_CHILD_LINE", "Line not valid: " + rawLine);
        }

        // name (obligatorio)
        String type = m.group("type");
        type = type.trim();
        if (type.isEmpty()) type = null;

        // count (opcional)
        String count = m.group("count");
        return new ChildLine(count, type, lineNumber, rawLine);
    }
}

final class ChildLine {
    private final String type;
    private final Integer min;
    private final Integer max;

    public ChildLine(String count, String type, int numLine, String rawLine) {
        this.type = type;
        
		if (count == null || count.isEmpty() || count.equals("*")) {
			min = null;
			max = null;
		} else if (count.equals("?")) {
			min = null;
			max = 1;
		} else if (count.equals("+")) {
			min = 1;
			max = null;
		} else if (count.endsWith("+")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			min = expectedNum;
			max = null;
		} else if (count.endsWith("-")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			min = null;
			max = expectedNum;
		} else {
            try {
                int expectedNum = Integer.parseInt(count);
    			min = expectedNum;
    			max = expectedNum;
            } catch (NumberFormatException ex) {
                throw new ParseException(numLine, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);
            }
		}
    }

    public String getType() {
        return type;
    }
    public Integer getMin() {
    	return min;
    }
    public Integer getMax() {
    	return max;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChildLine [type=");
		builder.append(type);
		builder.append(", min=");
		builder.append(min);
		builder.append(", max=");
		builder.append(max);
		builder.append("]");
		return builder.toString();
	}    
}

