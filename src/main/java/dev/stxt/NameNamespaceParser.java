package dev.stxt;

import java.util.Locale;

import dev.stxt.exceptions.ParseException;

/** Extracts the name and the namespace {@code (a.b.c)} from the left-hand side of an STXT line. */
public final class NameNamespaceParser {
    private NameNamespaceParser() {
        // Utility
    }

    /**
     * @param rawName raw name, with the namespace in parentheses if it carries one.
     * @param inheritedNs namespace inherited from the parent, used when {@code rawName} brings none of its own.
     * @param lineNumber line number, for the error messages.
     * @param fullLine original full line, for the error messages.
     * @return the name and the namespace, already split apart and resolved.
     * @throws ParseException if the name or the namespace are not well formed.
     */
    public static NameNamespace parse(String rawName, String inheritedNs, int lineNumber, String fullLine) {
        if (rawName == null) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        rawName = rawName.trim();
        int openIndex  = rawName.indexOf("(");
        int closeIndex = rawName.indexOf(")");

        // Both of them found
        String name = null;
        String namespace = inheritedNs;
        if (namespace == null) namespace = "";
        
        if (openIndex != -1 && closeIndex != -1)
        {
        	if (openIndex > closeIndex || closeIndex != rawName.length()-1)
        		throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Line not valid: " + fullLine);

        	name = rawName.substring(0, openIndex).trim();
        	// No trim here: the grammar (STXT-SPEC 7/16) allows no spaces inside '( )'
        	namespace = rawName.substring(openIndex+1, closeIndex);
        	
        	if (namespace.isEmpty())
        		throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Line not valid: " + fullLine);
        }
        else if (openIndex == -1 && closeIndex == -1)
        {
        	name = rawName;
        }
        else
        {
        	throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Line not valid: " + fullLine);
        }
        
        return new NameNamespace(name, namespace.toLowerCase(Locale.ROOT));
    }
}
