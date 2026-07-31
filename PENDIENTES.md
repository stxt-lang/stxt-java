# PENDIENTES

Un punto por tarea; se borran al resolverse y **este fichero se elimina cuando quede vacío** (mismo
criterio que el repaso de conformidad cerrado el 2026-07-28).

La publicación de `dev.stxt:stxt-core` en Maven Central **ya está hecha**: 0.5.2 el 2026-07-30 desde
el tag `v0.5.2`, y 0.5.3 el 2026-07-31 desde el tag `v0.5.3` (anotado y firmado). El porqué del flujo
(clave de firma, perfil `release`, plugin del Portal, inmutabilidad) está en la sección "Publicación
en Maven Central" de [CLAUDE.md](CLAUDE.md), el paso a paso en [RELEASING.md](RELEASING.md), y el
historial de cómo se llegó ahí, en el `git log`.

---

1. **Decidir la numeración respecto a `@stxt-lang/core`.** La 0.5.3 de Java se publicó el 2026-07-31
   sin equivalente en npm, que sigue en 0.5.2, así que ahora mismo **un mismo número no significa lo
   mismo en las dos implementaciones** — justo la señal de desalineación que avisa
   [CLAUDE.md](CLAUDE.md). El contenido de la 0.5.3 es solo documentación (comentarios y javadoc en
   inglés, resumen en todos los miembros públicos), sin ningún cambio de comportamiento, así que las
   dos implementaciones siguen de acuerdo en lo que importa.

   Las salidas razonables son dos: publicar una 0.5.3 espejo en `stxt-js` con su propio repaso de
   documentación, o aceptar que los números de parche pueden ir por libre y dejar la regla de
   alineación solo para minor/major, corrigiendo entonces la redacción de `CLAUDE.md`. Pendiente de
   decidir; hasta entonces, la divergencia está aquí anotada a propósito.

2. **Enlazar el artefacto desde `../stxt-web`**, para que las tres implementaciones se referencien
   entre sí. El README de `../stxt-js` ya lo enlaza (hecho el 2026-07-30), pero en `stxt-web` **no
   hay hoy ningún sitio donde ponerlo**: no tiene README, no hay página de herramientas ni de
   implementaciones, y en `es/_index.stxt` la entrada de navegación `Link: Herramientas` está
   comentada. Ni la extensión de VSCode ni el paquete npm están enlazados desde el sitio.

   Hacerlo bien implica crear una página nueva (canónica en `es/` + espejo en `en/`), descomentar esa
   entrada de navegación y regenerar el portal con `../stxt-cms` — decisión de contenido del sitio
   normativo, no un enlace suelto. Pendiente de decidir alcance.

## Mejoras opcionales, no bloqueantes

3. **Publicación automatizada** con GitHub Actions al crear un tag. Descartada de momento a favor
   del flujo manual; si se retoma, tener en cuenta dos cosas: en CI **no está `../stxt-web`** y las
   suites de corpus se saltarían (justo la red de seguridad que valida la conformidad), y la firma
   exige clave y passphrase en secretos del repositorio, hoy deliberadamente fuera de todo fichero.
