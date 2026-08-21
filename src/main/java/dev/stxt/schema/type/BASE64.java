package dev.stxt.schema.type;

import java.util.Base64;
import java.util.regex.Pattern;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code BASE64} type: checks that the content is valid Base64. */
public final class BASE64 implements Type {
	// STXT-SCHEMA-SPEC 9.5: the standard alphabet, in groups of four, with optional "=" padding
	private static final Pattern P_BASE64 = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2,3})?$");

	/** Single instance of this type. */
	public static final BASE64 INSTANCE = new BASE64();

	private BASE64() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
	public void validate(NodeDefinition ndef, Node n) {
		String raw = BinaryValue.get(n);
		// The blanks are already removed (BinaryValue); anything outside the alphabet, and the
		// empty value, are rejected here before java.util.Base64 gets a chance to be lenient
		if (raw.isEmpty() || !P_BASE64.matcher(raw).matches())
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' Invalid Base64");
		try {
			byte[] decoded = Base64.getDecoder().decode(raw);
			// Re-encode it so partially valid strings are not accepted
			// (e.g. leftover bits in the last encoded block)
			String reencoded = Base64.getEncoder().encodeToString(decoded);
			if (!stripPadding(raw).equals(stripPadding(reencoded))) {
				throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' Invalid Base64");
			}
		} catch (IllegalArgumentException e) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Node '" + n.getName() + "' Invalid Base64");
		}
	}

	private static String stripPadding(String s) {
		int end = s.length();
		while (end > 0 && s.charAt(end - 1) == '=')
			end--;
		return s.substring(0, end);
	}
}
