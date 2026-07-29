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
Portal). Falta todavía generar el **user token** de publicación (se hace en
`https://central.sonatype.com/usertoken`, normalmente ya con el namespace verificado) — pendiente
de usar en el punto 3 de abajo.

1. **Clave GPG**: no hay ninguna en la máquina (`gpg --list-secret-keys` estaba vacío la última vez
   que se comprobó; pendiente de reconfirmar en la máquina donde se retome). Hay que generarla,
   **publicarla en un keyserver público** (`keyserver.ubuntu.com`) y **guardar copia de seguridad de
   la clave privada y de la passphrase**: si se pierde, las versiones futuras habrá que firmarlas
   con otra clave. El cableado del perfil `release` ya está probado y falla justo aquí, con
   `gpg: no default secret key`.

2. **`~/.m2/settings.xml`**: no existe. Necesita un `<server>` con `<id>central</id>` y el token del
   Portal (generarlo en `https://central.sonatype.com/usertoken`, ya con el namespace verificado), y
   la passphrase de GPG (`gpg.passphrase`) o un agente que la aporte. **Fuera del repositorio**, es
   un fichero con credenciales.

3. **`central-publishing-maven-plugin`** en el `pom.xml` (`org.sonatype.central`), que es lo que
   sube el bundle al Portal. Va con `<publishingServerId>central</publishingServerId>` y conviene
   dejar `autoPublish` desactivado la primera vez, para poder inspeccionar el despliegue validado
   antes de confirmarlo — una vez publicado ya no se puede retirar.

4. **Ensayo y primera publicación**: `mvn -Prelease verify` para revisar el bundle en local y luego
   `mvn -Prelease deploy`. Comprobar después que el artefacto aparece en `central.sonatype.com` y,
   con algo más de retraso, en `search.maven.org` y `javadoc.io`.

5. **Documentar la release en el ecosistema** una vez publicada: enlazar el artefacto de Maven desde
   el README de `../stxt-js` (que hoy solo menciona npm y la extensión) y desde `../stxt-web`, para
   que las tres implementaciones se referencien entre sí.

## Mejoras opcionales, no bloqueantes

6. **Publicación automatizada** con GitHub Actions al crear un tag. Descartada de momento a favor
   del flujo manual; si se retoma, tener en cuenta que en CI **no está `../stxt-web`** y las suites
   de corpus se saltarían, que es justo la red de seguridad que valida la conformidad.
