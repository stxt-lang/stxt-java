# PENDIENTES

Un punto por tarea; se borran al resolverse y **este fichero se elimina cuando quede vacío** (mismo
criterio que el repaso de conformidad cerrado el 2026-07-28).

La publicación de `dev.stxt:stxt-core` en Maven Central **ya está hecha**: 0.5.2 publicada el
2026-07-30 desde el tag `v0.5.2`. Todo el contexto del flujo (clave de firma, perfil `release`,
plugin del Portal, pasos de una release futura) está en la sección "Publicación en Maven Central" de
[CLAUDE.md](CLAUDE.md); el historial de cómo se llegó ahí, en el `git log`.

---

1. **Enlazar el artefacto desde `../stxt-web`**, para que las tres implementaciones se referencien
   entre sí. El README de `../stxt-js` ya lo enlaza (hecho el 2026-07-30), pero en `stxt-web` **no
   hay hoy ningún sitio donde ponerlo**: no tiene README, no hay página de herramientas ni de
   implementaciones, y en `es/_index.stxt` la entrada de navegación `Link: Herramientas` está
   comentada. Ni la extensión de VSCode ni el paquete npm están enlazados desde el sitio.

   Hacerlo bien implica crear una página nueva (canónica en `es/` + espejo en `en/`), descomentar esa
   entrada de navegación y regenerar el portal con `../stxt-cms` — decisión de contenido del sitio
   normativo, no un enlace suelto. Pendiente de decidir alcance.

## Mejoras opcionales, no bloqueantes

2. **Publicación automatizada** con GitHub Actions al crear un tag. Descartada de momento a favor
   del flujo manual; si se retoma, tener en cuenta dos cosas: en CI **no está `../stxt-web`** y las
   suites de corpus se saltarían (justo la red de seguridad que valida la conformidad), y la firma
   exige clave y passphrase en secretos del repositorio, hoy deliberadamente fuera de todo fichero.
