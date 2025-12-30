package dev.stxt;

import java.util.Locale;
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
     *   - letras Unicode (acentos incluidos), números, espacios, '_' y '-'
     *   - no se permiten otros símbolos ni paréntesis
     *
     * namespace (opcional, al final):
     *   - opcional '@' al inicio
     *   - segmentos separados por '.'
     *   - cada segmento: [a-z0-9]+ (ASCII, minúsculas)
     *
     * Nota: el namespace se parsea case-insensitive pero se normaliza a minúsculas.
     */
    private static final String NAME_TOKEN = "[\\p{L}\\p{M}\\p{N}_-]+";
    private static final String NAME_PART  = NAME_TOKEN + "(?:\\s+" + NAME_TOKEN + ")*";

    private static final String NS_SEGMENT = "[a-z0-9]+";
    private static final String NAMESPACE  = "@?" + NS_SEGMENT + "(?:\\." + NS_SEGMENT + ")*";

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^\\s*(?<name>" + NAME_PART + ")(?:\\s*\\(\\s*(?<ns>" + NAMESPACE + ")\\s*\\))?\\s*$",
            Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE
    );

    private NameNamespaceParser() {
        // utility
    }

    public static NameNamespace parse(String rawName, String inheritedNs, int lineNumber, String fullLine) {
        if (rawName == null) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        Matcher m = LINE_PATTERN.matcher(rawName);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_NAMESPACE_DEF", "Line not valid: " + fullLine);
        }

        String name = m.group("name");
        if (name == null || name.isBlank()) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        String namespace = (inheritedNs != null) ? inheritedNs : Constants.EMPTY_NAMESPACE;

        String explicitNs = m.group("ns");
        if (explicitNs != null && !explicitNs.isBlank()) {
            namespace = explicitNs.toLowerCase(Locale.ROOT);
        }

        return new NameNamespace(name, namespace);
    }
}
