package dev.stxt.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ValidationException;

/** Parses the inline value of a child node inside an {@code @stxt.template}, shaped as {@code (min,max) TYPE [values]}. */
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
     * Parses the inline value of a child line.
     *
     * @param rawLine raw inline value of the child line.
     * @param lineNumber line number, for the error message.
     * @return the parsed child definition.
     * @throws ValidationException with code {@code STRUCTURE_LINE_NOT_VALID} if the format is not valid.
     */
    public static ChildLine parse(String rawLine, int lineNumber) {
    	if (rawLine.trim().isEmpty())
    		return new ChildLine(null, null, null, null);
    	
        Matcher m = CHILD_LINE_PATTERN.matcher(rawLine);
        if (!m.matches()) {
            throw new ValidationException(lineNumber, "STRUCTURE_LINE_NOT_VALID", "Line not valid: " + rawLine);
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
                throw new ValidationException(lineNumber, "CARDINALITY_NOT_VALID", "Invalid count " + count + " in line: " + rawLine);

            int minValue = parseCount(minMax[0].trim(), count, rawLine, lineNumber);
            int maxValue = parseCount(minMax[1].trim(), count, rawLine, lineNumber);

            // STXT-TEMPLATE-SPEC 7.1: in (min,max) it must hold that min <= max
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
            // -1 keeps the trailing empty item of "[a, b,]", which Java would otherwise drop
            String[] parts = valuesStr.split(",", -1);
            List<String> list = new ArrayList<>();
            for (String part: parts) {
                part = part.trim();
                // An empty item ("[a, , b]", "[a, b,]") is an error, as an empty Value: is in a
                // schema (STXT-TEMPLATE-SPEC 14.14). Only the whole list may be empty ("[]"),
                // which the template parser reports as VALUES_REQUIRED.
                if (part.isEmpty() && parts.length > 1)
                    throw new ValidationException(lineNumber, "VALUE_EMPTY", "Empty ENUM value in " + valuesStr);
                if (!part.isEmpty()) {
                    if (list.contains(part)) 
                        throw new ValidationException(lineNumber, "VALUE_DUPLICATED", "The values " + part + " is duplicated");
                    list.add(part);
                }
            }
            // Brackets being present (even empty ones, "[]") count as an explicit value
            // definition: we return a non-null array (possibly empty) to tell it apart from
            // brackets missing altogether (valuesStr == null, values stays null). That way
            // "[]" is treated as a real definition/redefinition.
            values = new String[list.size()];
            for (int i = 0; i<list.size(); i++)
                values[i] = list.get(i);
        }
		
        return new ChildLine(type, min, max, values);
    }

    // STXT-TEMPLATE-SPEC 7.1: num, min and max must be non-negative integers (no sign, no
    // leftover text); it throws CARDINALITY_NOT_VALID when they are not, instead of letting an
    // unwrapped NumberFormatException through
    private static int parseCount(String num, String count, String rawLine, int lineNumber) {
        if (!num.matches("\\d+"))
            throw new ValidationException(lineNumber, "CARDINALITY_NOT_VALID", "Invalid count " + count + " in line: " + rawLine);
        return Integer.parseInt(num);
    }
}