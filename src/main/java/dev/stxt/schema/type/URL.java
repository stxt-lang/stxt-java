package dev.stxt.schema.type;

import java.util.regex.Pattern;

/**
 * {@code URL} type: an absolute URL with a mandatory scheme and host, following the grammar of
 * STXT-SCHEMA-SPEC 9.4 — {@code scheme "://" [userinfo "@"] host [":" port] ["/" path]
 * ["?" query] ["#" fragment]} — and not {@link java.net.URI}, so every port accepts exactly the
 * same values. Any scheme of the form letter + letters/digits/{@code +}/{@code -}/{@code .} is
 * accepted; the host is non-empty (no TLD required, IPv6 in brackets, non-ASCII kept as it is);
 * a value without a scheme, a scheme without {@code //} and host ({@code mailto:}, {@code urn:},
 * {@code file:///}), inner blanks or a non-numeric port are rejected. Nothing is resolved or
 * normalised.
 */
public final class URL extends RegexValue {
	private static final Pattern P_URL = Pattern.compile(
			"^[A-Za-z][A-Za-z0-9+.-]*://(?:[^ \\t/?#@]+@)?(?:\\[[0-9A-Fa-f:.]+\\]|[^ \\t/?#@:\\[\\]]+)(?::[0-9]+)?(?:/[^ \\t?#]*)?(?:\\?[^ \\t#]*)?(?:#[^ \\t]*)?$");

	/** Single instance of this type. */
	public static final URL INSTANCE = new URL();

	private URL() {
		super(P_URL, "Invalid URL");
	}

	@Override
	public String getName() {
		return "URL";
	}
}
