package dev.stxt.schema.type;

import java.net.URI;
import java.net.URISyntaxException;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

/** {@code URL} type: checks that the value is a syntactically valid URI/URL. */
public final class URL implements Type {
	/** Single instance of this type. */
	public static final URL INSTANCE = new URL();

	private URL() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
		// INLINE value form (STXT-SCHEMA-SPEC 9.4): the '>>' block is not accepted
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
