package dev.stxt.schema.type;

import java.util.regex.Pattern;

public final class NumberType extends RegexValueType {
    // Acepta:
    //  - 12, -12, +12
    //  - 12.34, 12., .34
    //  - 1e10, 1E10, -1.2e-3, .5e+2
    private static final Pattern P_NUMBER = Pattern.compile(
            "^[+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?$"
    );

    public static final NumberType INSTANCE = new NumberType();

    private NumberType() {
        super(P_NUMBER, "Invalid number");
    }

	@Override
	public String getName() {
		return "NUMBER";
	}
}
