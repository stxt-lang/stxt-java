# PENDIENTES

Preparación de `dev.stxt:stxt-core` para publicar en Maven Central. Un punto por tarea; se borran
al resolverse y **este fichero se elimina cuando quede vacío** (mismo criterio que el repaso de
conformidad cerrado el 2026-07-28).

Contexto y decisiones ya tomadas: sección "Publicación en Maven Central" de [CLAUDE.md](CLAUDE.md).
Lo ya hecho (metadatos del POM, jars de sources/javadoc, perfil `release` con GPG, ajustes de build,
LICENSE, README y CHANGELOG) está en el `git log`; aquí solo queda lo que falta.

---

Hecho (2026-07-29): cuenta creada en el Central Portal (`central.sonatype.com`) y namespace
`dev.stxt` registrado y **verificado** (TXT en el DNS de `stxt.dev`, ya propagado y validado por el
Portal).

Hecho (2026-07-30): **clave GPG** generada, publicada en `keyserver.ubuntu.com` y respaldada — key ID
`AAC9F568A98BE7F0`, RSA 4096, sin caducidad. Detalles en CLAUDE.md. El respaldo (privada +
certificado de revocación) se verificó restaurándolo en un anillo temporal aislado, no solo
comprobando que los ficheros existieran.

Hecho (2026-07-30): **`central-publishing-maven-plugin`** 0.11.0 añadido al perfil `release` del
[pom.xml](pom.xml), con `extensions=true`, `autoPublish=false` y `waitUntil=VALIDATED`.

1. **`~/.m2/settings.xml`**: no existe. Solo necesita un `<server>` con `<id>central</id>` y el
   **user token** del Portal (se genera en `https://central.sonatype.com/usertoken`; es un par
   usuario/contraseña generado, no las credenciales de la cuenta). **Fuera del repositorio**, es un
   fichero con credenciales.

   La passphrase de GPG **no va aquí**: en maven-gpg-plugin 3.2.7 los parámetros `passphrase` y
   `passphraseServerId` están marcados como deprecated ("may leak sensitive information") y
   `useAgent` viene a `true`, así que la pide gpg-agent por pinentry en cada firma. Al ser la
   publicación manual, ese prompt interactivo es aceptable y mantiene la passphrase fuera del disco.

2. **Ensayo y primera publicación**: `mvn -Prelease verify` para revisar el bundle firmado en local
   (4 ficheros: pom y 3 jars, cada uno con su `.asc`) y luego `mvn -Prelease deploy`. Con
   `autoPublish=false` el despliegue queda validado y pendiente de confirmar a mano en el Portal.
   Comprobar después que el artefacto aparece en `central.sonatype.com` y, con algo más de retraso,
   en `search.maven.org` y `javadoc.io`.

3. **Documentar la release en el ecosistema** una vez publicada: enlazar el artefacto de Maven desde
   el README de `../stxt-js` (que hoy solo menciona npm y la extensión) y desde `../stxt-web`, para
   que las tres implementaciones se referencien entre sí.

## Mejoras opcionales, no bloqueantes

4. **Publicación automatizada** con GitHub Actions al crear un tag. Descartada de momento a favor
   del flujo manual; si se retoma, tener en cuenta que en CI **no está `../stxt-web`** y las suites
   de corpus se saltarían, que es justo la red de seguridad que valida la conformidad.
