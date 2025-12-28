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



