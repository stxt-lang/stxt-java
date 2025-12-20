package dev.stxt.resources.os;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Construye la lista de roots STXT según configuración, variables de entorno y
 * valores por defecto del OS.
 */
final class StxtRootsBuilder {
	private static final String STXT_EXTENSION = ".stxt";

	private StxtRootsBuilder() {
		// Utility class
	}

	static List<Path> buildStxtRoots(PathResolver.Config config) {
		if (config == null) {
			config = PathResolver.Config.empty();
		}

		int os = OsDetector.detectOsType();
		Path userHome = OsDetector.detectUserHome();
		Path projectRoot = determineProjectRoot(config);

		List<Path> roots = new ArrayList<>();

		// 1) Extra roots explícitos
		roots.addAll(config.getExtraRoots());

		// 2) STXT_HOME
		String stxtHomeEnv = System.getenv("STXT_HOME");
		if (stxtHomeEnv != null && !stxtHomeEnv.isBlank()) {
			roots.add(Paths.get(stxtHomeEnv));
		}

		// 3) STXT_PATH
		String stxtPathEnv = System.getenv("STXT_PATH");
		roots.addAll(OsDetector.splitPathList(stxtPathEnv, os));

		// 4) Roots por defecto según OS
		roots.addAll(buildDefaultRoots(os, projectRoot, userHome));

		// 5) Eliminar duplicados manteniendo orden
		roots = removeDuplicatesPreservingOrder(roots);

		return roots;
	}

	private static Path determineProjectRoot(PathResolver.Config config) {
		Path projectRoot = config.getProjectRoot();
		if (projectRoot != null) {
			return projectRoot;
		}
		return Paths.get("").toAbsolutePath();
	}

	private static List<Path> buildDefaultRoots(int os, Path projectRoot, Path userHome) {
		List<Path> roots = new ArrayList<>();

		if (os == OsDetector.POSIX) {
			// 1. Local al proyecto: ./.stxt
			roots.add(projectRoot.resolve(STXT_EXTENSION));

			// 2. Local al usuario: ~/.stxt (solo si conocemos userHome)
			if (userHome != null) {
				roots.add(userHome.resolve(STXT_EXTENSION));
			}

			// 3. Global del sistema: /etc/stxt
			roots.add(Paths.get("/etc/stxt"));
		} else if (os == OsDetector.WINDOWS) {
			// 1. Local al proyecto: .\.stxt
			roots.add(projectRoot.resolve(STXT_EXTENSION));

			// 2. Local al usuario: %APPDATA%\stxt (si existe)
			String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isBlank()) {
				roots.add(Paths.get(appData).resolve("stxt"));
			} else if (userHome != null) {
				// fallback a <userHome>/.stxt si no hay APPDATA
				roots.add(userHome.resolve(STXT_EXTENSION));
			}

			// 3. Global del sistema: %PROGRAMDATA%\stxt (si existe)
			String programData = System.getenv("PROGRAMDATA");
			if (programData != null && !programData.isBlank()) {
				roots.add(Paths.get(programData).resolve("stxt"));
			}
		}

		return roots;
	}

	private static List<Path> removeDuplicatesPreservingOrder(List<Path> input) {
		Set<Path> set = new LinkedHashSet<>(input);
		return new ArrayList<>(set);
	}
}
