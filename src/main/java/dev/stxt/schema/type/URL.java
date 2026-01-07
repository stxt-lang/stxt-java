package dev.stxt.schema.type;

import java.net.URI;
import java.net.URISyntaxException;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Type;

public final class URL implements Type {
	public static final URL INSTANCE = new URL();

	private URL() {
	}

    @Override
    public String getName() {
        return INSTANCE.getClass().getSimpleName();
    }
    
	@Override
    public void validate(NodeDefinition ndef, Node n) {
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
