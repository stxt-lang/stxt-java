package dev.stxt.discovery;

import java.io.IOException;
import java.nio.file.Files;
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
				entries.add(new DiscoveryEntry(child, child.getFileName().toString(), Files.isDirectory(child)));
			}
		}
		return entries;
	}

	@Override
	public String readFile(Path path) throws IOException {
		return Files.readString(path);
	}
}
