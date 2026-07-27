package dev.stxt.schema.type;

import java.util.Base64;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class BASE64 implements Type {
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
		try {
			byte[] decoded = Base64.getDecoder().decode(raw);
			// Re-codificamos para evitar aceptar cadenas parcialmente válidas
			// (p.ej. bits sobrantes en el último bloque codificado)
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
