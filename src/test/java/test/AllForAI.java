package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllForAI {
	public static void main(String[] args) {
		try {
			String contenido = leerTodoSrcMainJava();
			System.out.println(contenido);
			FileUtils.writeStringToFile(contenido, new File("all.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lee de forma recursiva todos los ficheros bajo "src/main/java" (relativo al
	 * directorio de trabajo actual) y concatena su contenido en un solo String.
	 *
	 * Formato de salida por fichero:
	 * Fichero: ruta/relativa/desde/src/main/java/Nombre.java
	 * ```
	 * <contenido>
	 * ```
	 */
	public static String leerTodoSrcMainJava() throws IOException {
		Path root = Paths.get("src", "main", "java");

		if (!Files.exists(root) || !Files.isDirectory(root)) {
			return ""; // o lanzar excepción si prefieres
		}

		try (Stream<Path> paths = Files.walk(root)) {
			return paths
					.filter(Files::isRegularFile)
					.map(path -> {
						try {
							// Nombre de fichero relativo a src/main/java
							Path relative = root.relativize(path);
							String fileName = relative.toString().replace('\\', '/');

							byte[] bytes = Files.readAllBytes(path);
							String content = new String(bytes, StandardCharsets.UTF_8);

							String lineSep = System.lineSeparator();
							return "Fichero: " + fileName + lineSep
									+ "```" + lineSep
									+ content + lineSep
									+ "```" + lineSep + lineSep;
						} catch (IOException e) {
							System.err.println("No se ha podido leer el fichero: " + path + " -> " + e.getMessage());
							return "";
						}
					})
					// ya incluyen saltos de línea al final de cada bloque
					.collect(Collectors.joining());
		}
	}
}