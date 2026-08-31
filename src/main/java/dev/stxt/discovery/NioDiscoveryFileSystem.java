package dev.stxt.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** {@link DiscoveryFileSystem} over the real file system ({@code java.nio.file}). */
public final class NioDiscoveryFileSystem implements DiscoveryFileSystem {

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
				entries.add(new DiscoveryEntry(child, child.getFileName().toString(),
						Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)));
			}
		}
		return entries;
	}

	@Override
	public String readFile(Path path) throws IOException {
		// Files.readString is a strict UTF-8 decode (STXT-SPEC 3): invalid bytes raise an
		// IOException instead of being silently substituted with U+FFFD.
		return Files.readString(path);
	}
}
