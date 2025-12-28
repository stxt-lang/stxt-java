package dev.stxt.schema.type;

import java.net.URI;
import java.net.URISyntaxException;

import dev.stxt.Node;
import dev.stxt.exceptions.ValidationException;
import dev.stxt.schema.Type;

public final class Url implements Type {
	public static final Url INSTANCE = new Url();

	private Url() {
	}

	@Override
	public void validate(Node n) {
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

	@Override
	public String getName() {
		return "URL";
	}
}
