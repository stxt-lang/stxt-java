package dev.stxt.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ValidationException;

/** Parsea el valor inline de un nodo hijo dentro de un {@code @stxt.template}, con la forma {@code (min,max) TIPO [valores]}. */
public final class ChildLineParser {

    private static final Pattern CHILD_LINE_PATTERN = Pattern.compile(
            "^\\s*" +
            "(?:\\(\\s*(?<count>[^()\\s][^)]*?)\\s*\\)\\s*)?" +
            "(?<type>[^\\[\\]()]*)?" +
            "(?:\\[\\s*(?<values>[^]]*?)\\s*\\]\\s*)?" +
            "\\s*$"
        );
    
    private ChildLineParser() {
    }

    /**
     * @param rawLine valor inline en bruto de la línea del hijo.
     * @param lineNumber número de línea, para el mensaje de error.
     * @return la definición de hijo parseada.
     * @throws ValidationException con código {@code INVALID_CHILD_LINE} si el formato no es válido.
     */
    public static ChildLine parse(String rawLine, int lineNumber) {
    	if (rawLine.trim().isEmpty())
    		return new ChildLine(null, null, null, null);
    	
        Matcher m = CHILD_LINE_PATTERN.matcher(rawLine);
        if (!m.matches()) {
            throw new ValidationException(lineNumber, "INVALID_CHILD_LINE", "Line not valid: " + rawLine);
        }

        String type = m.group("type");
        if (type != null) type = type.trim();
        if (type == null || type.isEmpty()) type = null;

        String count = m.group("count");
        Integer min = null;
        Integer max = null;
        
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
			min = parseCount(count.substring(0, count.length() - 1), count, rawLine, lineNumber);
			max = null;
		} else if (count.endsWith("-")) {
			min = null;
			max = parseCount(count.substring(0, count.length() - 1), count, rawLine, lineNumber);
		} else if (count.contains(",")) {
            String[] minMax = count.split(",", -1);
            if (minMax.length != 2)
                throw new ValidationException(lineNumber, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);

            int minValue = parseCount(minMax[0].trim(), count, rawLine, lineNumber);
            int maxValue = parseCount(minMax[1].trim(), count, rawLine, lineNumber);

            // STXT-TEMPLATE-SPEC 7.1: en (min,max) debe cumplirse min <= max
            if (minValue > maxValue)
                throw new ValidationException(lineNumber, "MIN_GREATER_THAN_MAX",
                        "Min " + minValue + " greater than Max " + maxValue + " in line: " + rawLine);

            min = minValue;
            max = maxValue;
		} else {
            int expectedNum = parseCount(count, count, rawLine, lineNumber);
			min = expectedNum;
			max = expectedNum;
		}
 
        String[] values = null;
        String valuesStr = m.group("values");
        if (valuesStr != null) {
            String[] parts = valuesStr.split(",");
            List<String> list = new ArrayList<>();
            for (String part: parts) {
                part = part.trim();
                if (!part.isEmpty()) {
                    if (list.contains(part)) 
                        throw new ValidationException(lineNumber, "VALUE_DUPLICATED", "The values " + part + " is duplicated");
                    list.add(part);
                }
            }
            // Los corchetes presentes (aunque vengan vacíos, "[]") cuentan como una
            // definición explícita de valores: devolvemos un array no-nulo (posiblemente
            // vacío) para distinguirlo de la ausencia total de corchetes (valuesStr == null,
            // values permanece null). Así "[]" se trata como redefinición/definición real.
            values = new String[list.size()];
            for (int i = 0; i<list.size(); i++)
                values[i] = list.get(i);
        }
		
        return new ChildLine(type, min, max, values);
    }

    // STXT-TEMPLATE-SPEC 7.1: num, min y max deben ser enteros no negativos (sin signo, sin
    // texto sobrante); lanza INVALID_CHILD_COUNT si no lo son, en vez de propagar
    // NumberFormatException sin envolver
    private static int parseCount(String num, String count, String rawLine, int lineNumber) {
        if (!num.matches("\\d+"))
            throw new ValidationException(lineNumber, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);
        return Integer.parseInt(num);
    }
}