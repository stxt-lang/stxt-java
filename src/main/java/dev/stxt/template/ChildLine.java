package dev.stxt.template;

import java.util.Arrays;

public class ChildLine {
    private final String type;
    private final Integer min;
    private final Integer max;
    private final String[] values;

    public ChildLine(String type, Integer min, Integer max, String[] values) {
		super();
		this.type = type;
		this.min = min;
		this.max = max;
		this.values = values;
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
    public String[] getValues() {
        return values;
    }

	@Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ChildLine [type=");
        builder.append(type);
        builder.append(", min=");
        builder.append(min);
        builder.append(", max=");
        builder.append(max);
        builder.append(", values=");
        builder.append(Arrays.toString(values));
        builder.append("]");
        return builder.toString();
    }    
}