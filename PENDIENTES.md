# PENDIENTES

Preparación de `dev.stxt:stxt-core` para publicar en Maven Central. Un punto por tarea; se borran
al resolverse y **este fichero se elimina cuando quede vacío** (mismo criterio que el repaso de
conformidad cerrado el 2026-07-28).

Contexto y decisiones ya tomadas: sección "Publicación en Maven Central" de [CLAUDE.md](CLAUDE.md).
Lo ya hecho (metadatos del POM, jars de sources/javadoc, perfil `release` con GPG, ajustes de build,
LICENSE, README y CHANGELOG) está en el `git log`; aquí solo queda lo que falta.

---

1. **Cuenta en el Central Portal** (`central.sonatype.com`). El OSSRH clásico (`oss.sonatype.org`)
   está retirado desde mediados de 2025; el flujo actual es el Portal. Requiere cuenta y generar un
   **token de publicación** (user token), que es lo que va en `settings.xml`, no la contraseña.

2. **Registrar y verificar el namespace `dev.stxt`**: el Portal da una clave y hay que publicar un
   **registro TXT en el DNS de `stxt.dev`**. Es el paso con más latencia (propagación DNS), aunque
   el trabajo en sí sean minutos. Alternativa si algún día se pierde el dominio:
   `io.github.stxt-lang`, verificable con un repo temporal en GitHub, pero obligaría a renombrar los
   packages Java para que groupId y `dev.stxt.*` sigan coincidiendo.

3. **Clave GPG**: no hay ninguna en la máquina (`gpg --list-secret-keys` está vacío). Hay que
   generarla, **publicarla en un keyserver público** (`keyserver.ubuntu.com`) y **guardar copia de
   seguridad de la clave privada y de la passphrase**: si se pierde, las versiones futuras habrá
   que firmarlas con otra clave. El cableado del perfil `release` ya está probado y falla justo
   aquí, con `gpg: no default secret key`.

4. **`~/.m2/settings.xml`**: no existe. Necesita un `<server>` con `<id>central</id>` y el token del
   Portal, y la passphrase de GPG (`gpg.passphrase`) o un agente que la aporte. **Fuera del
   repositorio**, es un fichero con credenciales.

5. **`central-publishing-maven-plugin`** en el `pom.xml` (`org.sonatype.central`), que es lo que
   sube el bundle al Portal. Va con `<publishingServerId>central</publishingServerId>` y conviene
   dejar `autoPublish` desactivado la primera vez, para poder inspeccionar el despliegue validado
   antes de confirmarlo — una vez publicado ya no se puede retirar.

6. **Ensayo y primera publicación**: `mvn -Prelease verify` para revisar el bundle en local y luego
   `mvn -Prelease deploy`. Comprobar después que el artefacto aparece en `central.sonatype.com` y,
   con algo más de retraso, en `search.maven.org` y `javadoc.io`.

7. **Documentar la release en el ecosistema** una vez publicada: enlazar el artefacto de Maven desde
   el README de `../stxt-js` (que hoy solo menciona npm y la extensión) y desde `../stxt-web`, para
   que las tres implementaciones se referencien entre sí.

## Mejoras opcionales, no bloqueantes

9. **Publicación automatizada** con GitHub Actions al crear un tag. Descartada de momento a favor
   del flujo manual; si se retoma, tener en cuenta que en CI **no está `../stxt-web`** y las suites
   de corpus se saltarían, que es justo la red de seguridad que valida la conformidad.
