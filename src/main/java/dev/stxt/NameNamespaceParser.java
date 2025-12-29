package dev.stxt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;

public final class NameNamespaceParser {

    /**
     * Acepta:
     *   - "nombre"
     *   - "nombre (namespace)"
     *
     * nombre:
     *   - letras unicode (acentos incluidos), números, espacios, '_' y '-'
     *   - no se permiten otros símbolos ni paréntesis
     *
     * namespace (opcional, al final):
     *   - opcional '@' al inicio
     *   - segmentos separados por '.'
     *   - cada segmento: letras unicode, números, '_' y '-'
     */
    private static final String NAME_TOKEN = "[\\p{L}\\p{M}\\p{N}_-]+";
    private static final String NAME_PART  = NAME_TOKEN + "(?:\\s+" + NAME_TOKEN + ")*";

    private static final String NS_SEGMENT = "[\\p{L}\\p{M}\\p{N}_-]+";
    private static final String NAMESPACE  = "@?" + NS_SEGMENT + "(?:\\." + NS_SEGMENT + ")*";

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^\\s*(" + NAME_PART + ")(?:\\s*\\(\\s*(" + NAMESPACE + ")\\s*\\))?\\s*$",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    private NameNamespaceParser() {
        // utility
    }

    public static NameNamespace parse(String rawName, String inheritedNs, int lineNumber, String fullLine) {
        if (rawName == null) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        // Si quieres preservar mayúsculas/minúsculas del nombre, NO lo normalices aquí.
        // Si lo quieres todo en minúsculas como antes, mantenlo:
        String normalized = rawName;

        Matcher m = LINE_PATTERN.matcher(normalized);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_NAMESPACE_DEF", "Line not valid: " + fullLine);
        }

        String name = m.group(1);
        if (name == null || name.isBlank()) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        String namespace = (inheritedNs != null) ? inheritedNs : Constants.EMPTY_NAMESPACE;
        String explicitNs = m.group(2);
        if (explicitNs != null && !explicitNs.isBlank()) {
            namespace = explicitNs;
        }

        return new NameNamespace(name, namespace);
    }
}
