package dev.stxt.discovery;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * {@link DiscoveryEnvironment} backed by the real process environment and the host OS
 * (STXT-DISCOVERY-SPEC section 4.2).
 */
public final class SystemDiscoveryEnvironment implements DiscoveryEnvironment {

	/** Creates an environment backed by the real process environment and the host OS. */
	public SystemDiscoveryEnvironment() {
	}

	@Override
	public List<String> getStxtPath() {
		String value = System.getenv("STXT_PATH");

		if (value == null) {
			return null;
		}
		if (value.isEmpty()) {
			return List.of();
		}
		return Arrays.asList(value.split(File.pathSeparator, -1));
	}

	@Override
	public Path getUserLevelDir() {
		String home = System.getProperty("user.home");
		return home == null ? null : Path.of(home, ".stxt");
	}

	@Override
	public Path getSystemLevelDir() {
		if (isWindows()) {
			String programData = System.getenv("ProgramData");
			return programData == null ? null : Path.of(programData, "stxt");
		}

		return Path.of("/etc/stxt");
	}

	private static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
	}
}
