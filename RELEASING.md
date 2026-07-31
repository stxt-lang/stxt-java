# Publicar una versión en Maven Central

Guía rápida del proceso completo **desde terminal, sin Eclipse**. Todo lo que hay aquí está
contrastado con [pom.xml](pom.xml); el porqué de cada decisión está en la sección "Publicación en
Maven Central" de [CLAUDE.md](CLAUDE.md).

Artefacto: **`dev.stxt:stxt-core`**, licencia MIT, Java 17, sin dependencias en runtime.
La versión va **alineada con `@stxt-lang/core`** (npm): el mismo número debe significar el mismo
comportamiento en las dos implementaciones.

---

## 0. Requisitos (comprobar una vez)

```bash
java -version                 # 17 o superior (se compila con release=17)
mvn -version
gpg --list-secret-keys --keyid-format LONG   # debe salir AAC9F568A98BE7F0
grep -c '<id>central</id>' ~/.m2/settings.xml   # 1 = el user token del Portal está puesto
ls ../stxt-web/.stxt          # necesario para que corran las suites de corpus
```

- La clave que firma es **`AAC9F568A98BE7F0`** (RSA 4096, sin caducidad). En el keyserver hay otra
  clave huérfana anterior (`7A221A776C45B26D`) que **no firma nada**: ignórala.
- La **passphrase no está en ningún fichero**, a propósito. La pide gpg-agent por pinentry.
- El `<server>` `central` de `~/.m2/settings.xml` lleva el **user token** del Portal, no la
  contraseña de la cuenta.

> **Los dos comandos con `-Prelease` hay que lanzarlos desde un terminal interactivo de verdad.**
> Desde una shell no interactiva (la de un agente, un script, un hook) la firma falla con
> `gpg: signing failed: No hay Pinentry`.

---

## 1. Preparar la versión

Cuatro sitios, siempre los cuatro:

| Qué | Dónde |
|---|---|
| `<version>` | [pom.xml](pom.xml) |
| `project.build.outputTimestamp` | [pom.xml](pom.xml) — si no se actualiza, el build deja de ser reproducible respecto a esa release |
| Versión en los snippets de Maven **y** Gradle | [README.md](README.md), sección de instalación (dos líneas) |
| Entrada nueva | [CHANGELOG.md](CHANGELOG.md), formato Keep a Changelog: pasar lo de `[Unreleased]` al número nuevo |

El README es **la portada del artefacto en Central y en javadoc.io**: sus ejemplos están compilados
y ejecutados contra el parser real y deben seguir estándolo.

---

## 2. Build y comprobaciones

```bash
mvn clean                    # IMPRESCINDIBLE, ver la nota de Eclipse más abajo
mvn test                     # toda la suite
mvn package                  # jar + sources + javadoc, sin firmar
```

Qué mirar en la salida de `mvn test`:

- **`Skipped: 0`**. Si sale algo saltado, `../stxt-web` no se ha encontrado y las suites de corpus
  —justo la red de seguridad que valida la conformidad con la spec— no se han ejecutado. Se fuerza
  la ruta con `STXT_WEB=/ruta mvn test`.
- Las tres suites de corpus deben aparecer: `CorpusDocumentsTest`, `CorpusSchemasTest`,
  `CorpusWriterTest`.

Y en la de `mvn package`, **cero avisos de javadoc**. Desde la 0.5.3 el javadoc sale limpio y esa es
la vara de medir: cualquier `warning` nuevo es documentación que se ha quedado a medias, y se ve en
javadoc.io. Ojo con el tope de la herramienta, que **corta la lista en 100 avisos** y engaña sobre el
total; para ver la cifra real:

```bash
javadoc -Xmaxwarns 10000 -quiet -d /tmp/jd -encoding UTF-8 -protected --release 17 \
    $(find src/main/java -name '*.java') 2>&1 | grep -c 'warning:'
```

Y comprobar que el bundle lleva los cuatro ficheros:

```bash
ls target/*.jar              # stxt-core-X.Y.Z.jar, -sources.jar, -javadoc.jar
unzip -p target/stxt-core-*.jar META-INF/MANIFEST.MF | grep Automatic-Module-Name   # dev.stxt
```

> ### Nota Eclipse (el motivo real del `mvn clean`)
> Si el proyecto se ha abierto alguna vez en Eclipse, m2e escribe su propio
> `target/classes/META-INF/MANIFEST.MF` con `Automatic-Module-Name: dev.stxt`. El
> maven-javadoc-plugin lo detecta, cambia a modo modular (`--patch-module`) y **el build del javadoc
> jar revienta** con:
>
> ```
> error: No source files for package dev.stxt
> ```
>
> No es un problema del código: `mvn clean` (o borrar ese MANIFEST) lo arregla. Como el javadoc jar
> es obligatorio en Central, esto tumbaría la release entera.

---

## 3. Firmar y subir

**Desde un terminal interactivo** (va a salir el diálogo de pinentry pidiendo la passphrase):

```bash
mvn -Prelease verify         # firma GPG de los 4 ficheros (pom + 3 jars)
mvn -Prelease deploy         # sube el bundle al Portal y espera a que lo valide
```

Qué hace exactamente `deploy`, y qué **no** hace:

- Sube el bundle, espera a la validación (`waitUntil=VALIDATED`) y lo deja **PENDIENTE**.
- Con `autoPublish=false`, **no publica nada**. Si la validación falla, falla el build, que es lo
  que se quiere: mejor que enterarse por la web.

---

## 4. Confirmar en el Portal (punto de no retorno)

1. Antes del clic, **tener el commit hecho**, para que el artefacto inmutable corresponda a un
   estado que está en git.
2. Entrar en <https://central.sonatype.com/publishing/deployments>.
3. Revisar el despliegue pendiente y darle a **Publish**.

- **Drop** descarta el despliegue y se puede reintentar con el mismo número de versión.
- Después del clic no hay vuelta atrás: **una release publicada en Central es inmutable**, no se
  puede borrar ni sustituir. Solo se puede publicar una versión nueva encima.
- Tarda un rato en aparecer en Central y bastante más en indexarse en search.maven.org.

---

## 5. Etiquetar en git

```bash
git tag -s vX.Y.Z -m "vX.Y.Z"     # anotado y firmado con AAC9F568A98BE7F0
git push origin vX.Y.Z
```

La 0.5.2 se etiquetó como `v0.5.2` pero *lightweight*. Desde la **0.5.3** el tag es anotado y
firmado, que es como deben hacerse los siguientes.

---

## Referencia rápida

```bash
mvn clean                    # 1. limpiar (obligatorio si se ha abierto Eclipse)
mvn test                     # 2. suite completa, Skipped debe ser 0
mvn package                  # 3. build sin firmar
mvn -Prelease verify         # 4. + firma GPG        (terminal interactivo)
mvn -Prelease deploy         # 5. sube y deja PENDIENTE (terminal interactivo)
#   6. clic manual en central.sonatype.com/publishing/deployments
git tag -s vX.Y.Z -m "vX.Y.Z" && git push origin vX.Y.Z          # 7.
```

---

## Si algo va mal

| Síntoma | Causa y arreglo |
|---|---|
| `gpg: signing failed: No hay Pinentry` | Shell no interactiva. Lánzalo desde un terminal de verdad. |
| `error: No source files for package dev.stxt` | El MANIFEST que deja Eclipse en `target/classes`. `mvn clean`. |
| `Tests run: ..., Skipped: N` con N > 0 | No encuentra `../stxt-web`. `STXT_WEB=/ruta mvn test`. |
| 401 / 403 al hacer `deploy` | User token del Portal caducado o mal puesto en `~/.m2/settings.xml`. |
| El Portal rechaza el bundle | Falta algún jar (sources/javadoc), falta una firma `.asc`, o falta metadato obligatorio en el pom (name, description, url, licencia, developers, scm). |
| La versión ya existe en Central | No se puede republicar. Sube el número de versión (y acuérdate de alinearlo con `@stxt-lang/core`). |
