package dev.stxt.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.stxt.exceptions.ParseException;

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

    public static ChildLine parse(String rawLine, int lineNumber) {
    	if (rawLine.trim().isEmpty())
    		return new ChildLine(null, null, null, null);
    	
        Matcher m = CHILD_LINE_PATTERN.matcher(rawLine);
        if (!m.matches()) {
            throw new ParseException(lineNumber, "INVALID_CHILD_LINE", "Line not valid: " + rawLine);
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
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			min = expectedNum;
			max = null;
		} else if (count.endsWith("-")) {
			int expectedNum = Integer.parseInt(count.substring(0, count.length() - 1));
			min = null;
			max = expectedNum;
		} else if (count.contains(",")) {
		    try {
	            String[] minMax = count.split(",");
	            min = Integer.parseInt(minMax[0].trim());
                max = Integer.parseInt(minMax[1].trim());
		    }
		    catch (Exception e) {
                throw new ParseException(lineNumber, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);
		    }
		} else {
            try {
                int expectedNum = Integer.parseInt(count);
    			min = expectedNum;
    			max = expectedNum;
            } catch (NumberFormatException ex) {
                throw new ParseException(lineNumber, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);
            }
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
                        throw new ParseException(lineNumber, "VALUE_DUPLICATED", "The values " + part + " is duplicated");
                    list.add(part);
                }
            }
            if (list.size()>0) {
                values = new String[list.size()];
                for (int i = 0; i<list.size(); i++)
                    values[i] = list.get(i);
            }            
        }
		
        return new ChildLine(type, min, max, values);
    }
}