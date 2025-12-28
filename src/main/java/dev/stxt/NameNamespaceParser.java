package dev.stxt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;

import static dev.stxt.utils.StringUtils.normalizeSimple;

public final class NameNamespaceParser {

    // `name (namespace)` donde namespace es opcional
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^\\s*([a-z0-9\\-_]+(?:\\s+[a-z0-9\\-_]+)*)(?:\\s*\\(\\s*(@?[a-z0-9]+(?:\\.[a-z0-9]+)*)\\s*\\))?\\s*$");

    private NameNamespaceParser() {
        // utility
    }

    /**
     * Parsea el texto de nombre completo y devuelve name + namespace lógico.
     *
     * @param rawName       Texto bruto (antes de normalizar) que viene de la línea.
     * @param inheritedNs   Namespace heredado del padre (puede ser vacío).
     * @param lineNumber    Número de línea (para `ParseException`).
     * @param fullLine      Línea completa para mensajes de error.
     */
    public static NameNamespace parse(
            String rawName,
            String inheritedNs,
            int lineNumber,
            String fullLine
    ) {
        if (rawName == null) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        String normalized = normalizeSimple(rawName);
        Matcher m = NAME_PATTERN.matcher(normalized);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_NAMESPACE_DEF", "Line not valid: " + fullLine);
        }

        String name = m.group(1);
        if (name == null || name.isEmpty()) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        String namespace = inheritedNs != null ? inheritedNs : Constants.EMPTY_NAMESPACE;
        if (m.group(2) != null) {
            namespace = m.group(2);
        }

        return new NameNamespace(name, namespace);
    }
}


