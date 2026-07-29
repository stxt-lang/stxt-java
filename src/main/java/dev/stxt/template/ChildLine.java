package dev.stxt.template;

import java.util.Arrays;

/** Una línea de definición de hijo dentro de un {@code @stxt.template}: tipo, cardinalidad y valores permitidos. */
public class ChildLine {
    private final String type;
    private final Integer min;
    private final Integer max;
    private final String[] values;

    /**
     * @param type nombre del tipo, o {@code null} si no se especifica.
     * @param min cardinalidad mínima, o {@code null}.
     * @param max cardinalidad máxima, o {@code null}.
     * @param values valores permitidos (ENUM), o {@code null} si no se restringen.
     */
    public ChildLine(String type, Integer min, Integer max, String[] values) {
		super();
		this.type = type;
		this.min = min;
		this.max = max;
		this.values = values;
	}
    
	/** @return nombre del tipo declarado, o {@code null} si no se especifica. */
	public String getType() {
        return type;
    }
    /** @return cardinalidad mínima, o {@code null} si no hay mínimo. */
    public Integer getMin() {
    	return min;
    }
    /** @return cardinalidad máxima, o {@code null} si no hay máximo. */
    public Integer getMax() {
    	return max;
    }
    /** @return valores permitidos (ENUM), o {@code null} si no se restringen. */
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