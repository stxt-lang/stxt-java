package dev.stxt.schema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.ParseException;

public final class ChildLineParser {

    private static final Pattern CHILD_LINE_PATTERN = Pattern.compile(
        "^\\s*" +
        "(?:\\(\\s*(?<count>[^()\\s][^)]*?)\\s*\\)\\s*)?" + // (count)
        "(?<name>[^()]+?)" +                               // name
        "\\s*(?:\\(\\s*(?<ns>[^()]+?)\\s*\\))?" +          // (namespace)
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
            String inheritedNs,
            int lineNumber
    ) {
        if (rawLine == null) {
            throw new ParseException(lineNumber, "INVALID_CHILD_LINE", "Line not valid: null");
        }

        Matcher m = CHILD_LINE_PATTERN.matcher(rawLine);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_CHILD_LINE", "Line not valid: " + rawLine);
        }

        // name (obligatorio)
        String name = m.group("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ParseException(lineNumber, "INVALID_CHILD_LINE", "Missing name in line: " + rawLine);
        }
        name = name.trim();

        // count (opcional)
        String countStr = m.group("count");
        Integer count = null;
        if (countStr != null && !countStr.trim().isEmpty()) {
            try {
                count = Integer.parseInt(countStr.trim());
            } catch (NumberFormatException ex) {
                throw new ParseException(lineNumber, "INVALID_CHILD_COUNT", "Invalid count " + countStr + " in line: " + rawLine);
            }
        }

        // namespace: o bien el explícito, o el heredado
        String namespace = m.group("ns");
        if (namespace != null) {
            namespace = namespace.trim();
        } else {
            namespace = inheritedNs;
        }

        return new ChildLine(count, name, namespace);
    }
    
	public static void updateCount(SchemaChild child, String count) {
		if (count == null || count.isEmpty() || count.equals("*")) {
			// No min, no max para empty y "*"
		} else if (count.equals("?")) {
			child.setMax(1);
		} else if (count.equals("+")) {
			child.setMin(1);
		} else if (count.endsWith("+")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			child.setMin(expectedNum);
		} else if (count.endsWith("-")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			child.setMax(expectedNum);
		} else {
			int expectedNum = Integer.parseInt(count);
			child.setMin(expectedNum);
			child.setMax(expectedNum);
		}
	}
    
}

final class ChildLine {
    private final Integer count;    // puede ser null si no se especifica
    private final String name;
    private final String namespace; // puede ser null o vacío

    public ChildLine(Integer count, String name, String namespace) {
        this.count = count;
        this.name = name;
        this.namespace = namespace;
    }

    public Integer getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }
}

