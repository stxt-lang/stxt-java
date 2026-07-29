package dev.stxt.runtime;

import java.util.List;

import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderCache;
import dev.stxt.schema.SchemaProviderResources;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.TemplateSchemaProvider;

/**
 * Fachada de uso habitual de STXT. {@link #parser(ResourcesLoader)} da un {@link Parser} ya
 * configurado con validación de schemas y templates (con caché); {@link #rawParser()} da un
 * parser sin validación (solo sintaxis).
 */
public final class STXT {
    private STXT() {}

    /**
     * @param loader de dónde cargar los schemas/templates.
     * @return un parser con validación de schema ya registrada.
     */
    public static Parser parser(ResourcesLoader loader) {
        Parser p = new Parser();
        p.registerValidator(new SchemaValidator(schemaProvider(loader)));
        return p;
    }

    /** @return un parser sin ningún validador registrado (solo valida sintaxis). */
    public static Parser rawParser() {
        return new Parser();
    }

    /**
     * @param loader de dónde cargar los schemas/templates.
     * @return un {@link SchemaProvider} que combina schemas y templates, con caché.
     */
    public static SchemaProvider schemaProvider(ResourcesLoader loader) {
        return new SchemaProviderCache(List.of(
            new SchemaProviderResources(loader),
            new TemplateSchemaProvider(loader)
        ));
    }
}
