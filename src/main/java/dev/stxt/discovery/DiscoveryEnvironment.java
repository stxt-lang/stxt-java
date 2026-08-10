package dev.stxt.discovery;

import java.nio.file.Path;
import java.util.List;

/**
 * Environment abstraction used by {@link DiscoveryResolver} (STXT-DISCOVERY-SPEC).
 *
 * It answers the three questions that depend on the process environment and the host OS:
 * the {@code STXT_PATH} override, the user-level directory and the system-level directory.
 * Injecting it keeps {@link DiscoveryResolver} deterministic and testable without touching
 * the real environment; {@link SystemDiscoveryEnvironment} is the implementation backed by it.
 */
public interface DiscoveryEnvironment {
	/**
	 * The value of the {@code STXT_PATH} environment variable, already split into entries.
	 *
	 * The distinction between "not defined" and "defined but empty" is normative
	 * (STXT-DISCOVERY-SPEC section 6): when defined, {@code STXT_PATH} completely replaces
	 * the resolution chain, and an empty value leaves the chain empty.
	 *
	 * @return the list of directories (highest precedence first), an empty list when the
	 *         variable is defined but empty, or {@code null} when it is not defined at all.
	 */
	List<String> getStxtPath();

	/**
	 * The user-level resolution directory ({@code $HOME/.stxt} on Unix,
	 * {@code %USERPROFILE%\.stxt} on Windows).
	 *
	 * @return the user-level directory, or {@code null} when the host has no user home.
	 */
	Path getUserLevelDir();

	/**
	 * The system-level resolution directory ({@code /etc/stxt} on Unix,
	 * {@code %ProgramData%\stxt} on Windows).
	 *
	 * @return the system-level directory, or {@code null} when the host has no system level.
	 */
	Path getSystemLevelDir();
}
