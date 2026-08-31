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
				// Do not follow directory symbolic links (STXT-DISCOVERY-SPEC section 3 and
				// section 10): a symlink whose target is a directory is omitted from the
				// listing entirely, so the resolver's recursive descent cannot be lured into
				// a symlink loop (e.g. .stxt/loop -> ..) or into an unrelated tree. A symlink
				// to a regular file still lists as a file; a real directory lists as one.
				if (Files.isSymbolicLink(child)) {
					if (Files.isDirectory(child))	// follows the link: target is a directory
						continue;
					entries.add(new DiscoveryEntry(child, child.getFileName().toString(), false));
				} else {
					entries.add(new DiscoveryEntry(child, child.getFileName().toString(),
							Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)));
				}
			}
		}
		return entries;
	}

	@Override
	public String readFile(Path path) throws IOException {
		return Files.readString(path);
	}
}
