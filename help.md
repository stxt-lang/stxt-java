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
