package dev.stxt;

import java.util.Locale;

import dev.stxt.exceptions.ParseException;

public final class NameNamespaceParser {
    private NameNamespaceParser() {
        // Utility
    }

    public static NameNamespace parse(String rawName, String inheritedNs, int lineNumber, String fullLine) {
        if (rawName == null) {
            throw new ParseException(lineNumber, "INVALID_LINE", "Line not valid: " + fullLine);
        }

        rawName = rawName.trim();
        int indexInicio = rawName.indexOf("(");
        int indexFin    = rawName.indexOf(")");
        
        // Encontrados los dos
        String name = null;
        String namespace = inheritedNs;
        if (namespace == null) namespace = "";
        
        if (indexInicio != -1 && indexFin != -1)
        {
        	if (indexInicio > indexFin || indexFin != rawName.length()-1) 
        		throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Line not valid: " + fullLine);
        	
        	name = rawName.substring(0, indexInicio).trim();
        	namespace = rawName.substring(indexInicio+1, indexFin).trim();
        	
        	if (namespace.isEmpty())
        		throw new ParseException(lineNumber, "INVALID_NAMESPACE", "Line not valid: " + fullLine);
        }
        else if (indexInicio == -1 && indexFin == -1)
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
