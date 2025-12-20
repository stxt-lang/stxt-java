package dev.stxt.resources.os;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Detecta el tipo de sistema operativo y proporciona información específica del
 * OS.
 */
final class OsDetector {
	private OsDetector() {
		// Utility class
	}

	static final int POSIX = 0;
	static final int WINDOWS = 1;

	static int detectOsType() {
		String osName = System.getProperty("os.name", "").toLowerCase();
		if (osName.contains("win")) {
			return WINDOWS;
		}
		return POSIX;
	}

	static Path detectUserHome() {
		String home = System.getProperty("user.home");
		if (home == null || home.isBlank()) {
			return null;
		}
		Path path = Paths.get(home);
		if (Files.isDirectory(path)) {
			return path;
		}
		return null;
	}

	static List<Path> splitPathList(String raw, int os) {
		List<Path> result = new ArrayList<>();
		if (raw == null || raw.isBlank()) {
			return result;
		}

		char separator = (os == WINDOWS) ? ';' : ':';
		String[] parts = raw.split(String.valueOf(separator));

		for (String part : parts) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				result.add(Paths.get(trimmed));
			}
		}

		return result;
	}
}
