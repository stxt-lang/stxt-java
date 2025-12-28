package dev.stxt.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.ParseException;

public final class ChildLineParser {

    private static final Pattern CHILD_LINE_PATTERN = Pattern.compile(
            "^\\s*" +
            "(?:\\(\\s*(?<count>[^()\\s][^)]*?)\\s*\\)\\s*)?" +
            "(?<type>[^()]*)" +                                
            "\\s*$"
        );
    
    private ChildLineParser() {
    }

    public static ChildLine parse(String rawLine, int lineNumber) {
    	if (rawLine.trim().isEmpty())
    		return new ChildLine(null, null, null);
    	
        Matcher m = CHILD_LINE_PATTERN.matcher(rawLine);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_CHILD_LINE", "Line not valid: " + rawLine);
        }

        String type = m.group("type");
        type = type.trim();
        if (type.isEmpty()) type = null;

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
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			min = expectedNum;
			max = null;
		} else if (count.endsWith("-")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			min = null;
			max = expectedNum;
		} else {
            try {
                int expectedNum = Integer.parseInt(count);
    			min = expectedNum;
    			max = expectedNum;
            } catch (NumberFormatException ex) {
                throw new ParseException(lineNumber, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);
            }
		}
        
        return new ChildLine(type, min, max);
    }
}



