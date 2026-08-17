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
 * Usual entry point of STXT. {@link #parser(ResourcesLoader)} gives a {@link Parser} already
 * wired up with schema and template validation (cached); {@link #rawParser()} gives a parser
 * with no validation at all (syntax only).
 */
public final class STXT {
    private STXT() {}

    /**
     * Creates a parser with schema and template validation already registered. Only the nodes
     * with a namespace are validated (see {@link ConditionalValidator}): a document, or a root
     * node, without a namespace is not wrong, it just cannot be validated.
     *
     * @param loader where to load the schemas/templates from.
     * @return a parser with schema validation already registered.
     */
    public static Parser parser(ResourcesLoader loader) {
        Parser p = new Parser();
        p.registerValidator(new ConditionalValidator(new SchemaValidator(schemaProvider(loader))));
        return p;
    }

    /** {@return a parser with no validator registered (it only validates syntax)} */
    public static Parser rawParser() {
        return new Parser();
    }

    /**
     * Creates the schema provider used by {@link #parser(ResourcesLoader)}.
     *
     * @param loader where to load the schemas/templates from.
     * @return a {@link SchemaProvider} that combines schemas and templates, with a cache.
     */
    public static SchemaProvider schemaProvider(ResourcesLoader loader) {
        return new SchemaProviderCache(List.of(
            new SchemaProviderResources(loader),
            new TemplateSchemaProvider(loader)
        ));
    }
}
