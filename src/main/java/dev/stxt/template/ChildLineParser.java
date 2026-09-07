package dev.stxt.template;

import java.util.ArrayList;
import java.util.List;

import dev.stxt.Constants;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.utils.StringUtils;

/** Parses the inline value of a child node inside an {@code @stxt.template}, shaped as {@code (min,max) TYPE [values]}. */
public final class ChildLineParser {

    private ChildLineParser() {
    }

    /**
     * Splits a RuleSpec {@code (count) TYPE [values]} into its three optional parts by a
     * hand-written scan, not a regular expression: the pattern used until 2026-09-06 backtracked
     * in O(n³) on a line without the closing {@code ]} (a 10 000-character Structure line took
     * minutes, with no timeout possible in java.util.regex). Blanks are U+0020/U+0009 only
     * (STXT-TEMPLATE-SPEC 6.2/9), and the rules the pattern enforced are kept exactly: the count
     * runs to the first {@code )} and, trimmed, is neither empty nor starts with {@code (}; the
     * type may not contain {@code (}, {@code )} or {@code ]}; the values run from the first
     * {@code [} to the first {@code ]} after it, and only blanks may follow that {@code ]}.
     *
     * @return the trimmed parts (null each when absent), or null if the line has not that shape.
     */
    private static String[] splitRuleSpec(String rawLine) {
        int n = rawLine.length();
        int i = 0;
        while (i < n && StringUtils.isBlank(rawLine.charAt(i)))
            i++;

        String count = null;
        if (i < n && rawLine.charAt(i) == '(') {
            int close = rawLine.indexOf(')', i + 1);
            if (close == -1)
                return null;
            count = StringUtils.trim(rawLine.substring(i + 1, close));
            if (count.isEmpty() || count.charAt(0) == '(')
                return null;
            i = close + 1;
        }

        int open = rawLine.indexOf('[', i);
        String type = rawLine.substring(i, open == -1 ? n : open);
        if (type.indexOf('(') != -1 || type.indexOf(')') != -1 || type.indexOf(']') != -1)
            return null;
        type = StringUtils.trim(type);
        if (type.isEmpty())
            type = null;

        String values = null;
        if (open != -1) {
            int close = rawLine.indexOf(']', open + 1);
            if (close == -1)
                return null;
            values = StringUtils.trim(rawLine.substring(open + 1, close));
            for (int j = close + 1; j < n; j++)
                if (!StringUtils.isBlank(rawLine.charAt(j)))
                    return null;
        }

        return new String[] { count, type, values };
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
    	if (StringUtils.trim(rawLine).isEmpty())
    		return new ChildLine(null, null, null, null);
    	
        String[] rule = splitRuleSpec(rawLine);
        if (rule == null) {
            throw new ValidationException(lineNumber, "STRUCTURE_LINE_NOT_VALID", "Line not valid: " + rawLine);
        }

        String count = rule[0];
        String type = rule[1];
        String valuesStr = rule[2];
        Long min = null;
        Long max = null;

		if (count == null || count.isEmpty() || count.equals("*")) {
			// min and max stay null: no bounds
		} else if (count.equals("?")) {
			max = 1L;
		} else if (count.equals("+")) {
			min = 1L;
		} else if (count.endsWith("+")) {
			min = parseCount(count.substring(0, count.length() - 1), count, rawLine, lineNumber);
		} else if (count.endsWith("-")) {
			max = parseCount(count.substring(0, count.length() - 1), count, rawLine, lineNumber);
		} else if (count.contains(",")) {
            String[] minMax = count.split(",", -1);
            if (minMax.length != 2)
                throw new ValidationException(lineNumber, "CARDINALITY_NOT_VALID", "Invalid count " + count + " in line: " + rawLine);

            long minValue = parseCount(StringUtils.trim(minMax[0]), count, rawLine, lineNumber);
            long maxValue = parseCount(StringUtils.trim(minMax[1]), count, rawLine, lineNumber);

            // STXT-TEMPLATE-SPEC 7.1: in (min,max) it must hold that min <= max
            if (minValue > maxValue)
                throw new ValidationException(lineNumber, "MIN_GREATER_THAN_MAX",
                        "Min " + minValue + " greater than Max " + maxValue + " in line: " + rawLine);

            min = minValue;
            max = maxValue;
		} else {
            long expectedNum = parseCount(count, count, rawLine, lineNumber);
			min = expectedNum;
			max = expectedNum;
		}
 
        String[] values = null;
        if (valuesStr != null) {
            // -1 keeps the trailing empty item of "[a, b,]", which Java would otherwise drop
            String[] parts = valuesStr.split(",", -1);
            List<String> list = new ArrayList<>();
            for (String part: parts) {
                part = StringUtils.trim(part);
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
    // leftover text) bounded to 2^32 - 1 like Min/Max in a schema; it throws
    // CARDINALITY_NOT_VALID when they are not, instead of letting an unwrapped
    // NumberFormatException through (a literal too big for a long overflows, which implies
    // exceeding the bound)
    private static long parseCount(String num, String count, String rawLine, int lineNumber) {
        if (!num.matches("\\d+"))
            throw new ValidationException(lineNumber, "CARDINALITY_NOT_VALID", "Invalid count " + count + " in line: " + rawLine);
        long value;
        try {
            value = Long.parseLong(num);
        } catch (NumberFormatException e) {
            throw new ValidationException(lineNumber, "CARDINALITY_NOT_VALID", "Invalid count " + count + " in line: " + rawLine);
        }
        if (value > Constants.MAX_CARDINALITY)
            throw new ValidationException(lineNumber, "CARDINALITY_NOT_VALID", "Invalid count " + count + " in line: " + rawLine);
        return value;
    }
}