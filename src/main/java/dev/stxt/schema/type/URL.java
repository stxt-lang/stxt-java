package dev.stxt.schema.type;

import java.net.URI;
import java.net.URISyntaxException;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** Tipo {@code URL}: valida que el valor sea una URI/URL sintácticamente válida. */
public final class URL implements Type {
	/** Instancia única de este tipo. */
	public static final URL INSTANCE = new URL();

	private URL() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// Forma del valor INLINE (STXT-SCHEMA-SPEC 9.4): no admite bloque '>>'
		if (n.isTextNode()) {
			throw new ValidationException(n.getLine(), "NOT_ALLOWED_TEXT",
					"Not allowed text in node " + n.getQualifiedName());
		}

		String url = n.getValue();
		try {
			URI uri = new URI(url);
			boolean ok = uri.getScheme() != null && uri.getHost() != null;
			if (!ok)
				throw new IllegalArgumentException();
		} catch (URISyntaxException | IllegalArgumentException e) {
			throw new ValidationException(n.getLine(), "INVALID_VALUE", "Invalid URL: " + url);
		}
	}
}
