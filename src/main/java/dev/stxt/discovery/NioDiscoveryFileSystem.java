package dev.stxt.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import dev.stxt.Constants;

/** {@link DiscoveryFileSystem} over the real file system ({@code java.nio.file}). */
public final class NioDiscoveryFileSystem implements DiscoveryFileSystem {

	/**
	 * Largest definition file a resolution directory loads: the parser's default input limit
	 * in characters, times the 4 bytes a character takes at most in UTF-8.
	 */
	public static final long MAX_DEFINITION_FILE_BYTES = 4L * Constants.DEFAULT_MAX_INPUT_SIZE;

	/** Creates a file system over {@code java.nio.file}; it has no state. */
	public NioDiscoveryFileSystem() {
	}

	@Override
	public boolean isDirectory(Path path) {
		return Files.isDirectory(path);
	}

	@Override
	public List<DiscoveryEntry> listDirectory(Path path) throws IOException {
		List<DiscoveryEntry> entries = new ArrayList<>();
		try (Stream<Path> stream = Files.list(path)) {
			for (Path child : stream.toList()) {
				// Do not follow symbolic links at all (STXT-DISCOVERY-SPEC section 3 and
				// section 10): every symlink is omitted from the listing, so a resolution
				// directory loads only the real files it contains. A directory link could lure
				// the recursive descent into a loop (e.g. .stxt/loop -> ..) or into an unrelated
				// tree; a file link could read a file from outside the .stxt/ and leak its
				// content through a resolution error.
				if (Files.isSymbolicLink(child))
					continue;
				boolean directory = Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS);
				// A FIFO, socket or device is neither: reading it could block forever
				if (!directory && !Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS))
					continue;
				entries.add(new DiscoveryEntry(child, child.getFileName().toString(), directory));
			}
		}
		return entries;
	}

	@Override
	public String readFile(Path path) throws IOException {
		// A definition is parsed with the default limits (DEFAULT_MAX_INPUT_SIZE characters, at
		// most 4 bytes each in UTF-8), so a bigger file cannot be within them: rejected by size
		// before it is read whole, which kept the memory of a load proportional to the file
		// instead of to the limit (a 40 MB definition was an OutOfMemoryError escaping from
		// resolve()). The IOException becomes a DISCOVERY_NOT_PARSEABLE error.
		long size = Files.size(path);
		if (size > MAX_DEFINITION_FILE_BYTES)
			throw new IOException("Definition file larger than " + MAX_DEFINITION_FILE_BYTES + " bytes: " + path);
		// Files.readString is a strict UTF-8 decode (STXT-SPEC 3): invalid bytes raise an
		// IOException instead of being silently substituted with U+FFFD.
		return Files.readString(path);
	}
}
