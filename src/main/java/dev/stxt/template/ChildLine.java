package dev.stxt.template;

import dev.stxt.ParseException;

public class ChildLine {
    private final String type;
    private final Integer min;
    private final Integer max;

    public ChildLine(String count, String type, int numLine, String rawLine) {
        this.type = type;
        
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
                throw new ParseException(numLine, "INVALID_CHILD_COUNT", "Invalid count " + count + " in line: " + rawLine);
            }
		}
    }

    public String getType() {
        return type;
    }
    public Integer getMin() {
    	return min;
    }
    public Integer getMax() {
    	return max;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChildLine [type=");
		builder.append(type);
		builder.append(", min=");
		builder.append(min);
		builder.append(", max=");
		builder.append(max);
		builder.append("]");
		return builder.toString();
	}    
}