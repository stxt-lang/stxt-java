package dev.stxt.template;

public class ChildLine {
    private final String type;
    private final Integer min;
    private final Integer max;

    public ChildLine(String type, Integer min, Integer max) {
		super();
		this.type = type;
		this.min = min;
		this.max = max;
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