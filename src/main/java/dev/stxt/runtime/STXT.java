package dev.stxt.runtime;

import java.util.List;

import dev.stxt.Parser;
import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderCache;
import dev.stxt.schema.SchemaProviderResources;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.TemplateSchemaProvider;

public final class STXT {
    private STXT() {}

    /**
     * Standard parser: validates nodes using schemas and templates loaded from resources.
     * Policy: @stxt.schema overrides @stxt.template if both exist for the same namespace.
     */
    public static Parser parser(ResourcesLoader loader) {
        Parser p = new Parser();
        p.registerValidator(new SchemaValidator(schemaProvider(loader)));
        return p;
    }

    /** Core parser without validators/providers. */
    public static Parser rawParser() {
        return new Parser();
    }

    public static SchemaProvider schemaProvider(ResourcesLoader loader) {
        return new SchemaProviderCache(List.of(
            new SchemaProviderResources(loader),
            new TemplateSchemaProvider(loader)
        ));
    }
}
