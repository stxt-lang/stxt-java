package dev.stxt.template;

import java.util.Arrays;

/** A child definition line inside an {@code @stxt.template}: type, cardinality and allowed values. */
public class ChildLine {
    private final String type;
    // Long, not Integer: the bound of a cardinality is 2^32 - 1 (STXT-TEMPLATE-SPEC 7.1),
    // which does not fit a signed int
    private final Long min;
    private final Long max;
    private final String[] values;

    /**
     * Creates a child definition line.
     *
     * @param type name of the type, or {@code null} if it is not given.
     * @param min minimum cardinality, or {@code null}.
     * @param max maximum cardinality, or {@code null}.
     * @param values allowed values (ENUM), or {@code null} if they are not restricted.
     */
    public ChildLine(String type, Long min, Long max, String[] values) {
		super();
		this.type = type;
		this.min = min;
		this.max = max;
		this.values = values;
	}
    
	/** {@return the name of the declared type, or {@code null} if it is not given} */
	public String getType() {
        return type;
    }
    /** {@return the minimum cardinality, or {@code null} if there is no minimum} */
    public Long getMin() {
    	return min;
    }
    /** {@return the maximum cardinality, or {@code null} if there is no maximum} */
    public Long getMax() {
    	return max;
    }
    /** {@return the allowed values (ENUM), or {@code null} if they are not restricted} */
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