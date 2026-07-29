package dev.stxt;

import java.util.Locale;

import dev.stxt.exceptions.ParseException;

/** Extrae el nombre y el namespace {@code (a.b.c)} de la parte izquierda de una línea STXT. */
public final class NameNamespaceParser {
    private NameNamespaceParser() {
        // Utility
    }

    /**
     * @param rawName nombre en bruto, con el namespace entre paréntesis si lo tiene.
     * @param inheritedNs namespace heredado del padre, usado si {@code rawName} no trae el suyo propio.
     * @param lineNumber número de línea, para los mensajes de error.
     * @param fullLine línea completa original, para los mensajes de error.
     * @return el nombre y el namespace ya separados y resueltos.
     * @throws ParseException si el nombre o el namespace no tienen un formato válido.
     */
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
        	// Sin trim: la gramática (STXT-SPEC 7/16) no admite espacios dentro de '( )'
        	namespace = rawName.substring(indexInicio+1, indexFin);
        	
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
