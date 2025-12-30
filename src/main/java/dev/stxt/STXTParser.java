package dev.stxt;

import java.util.ArrayList;
import java.util.List;

import dev.stxt.resources.ResourcesLoader;
import dev.stxt.schema.SchemaProvider;
import dev.stxt.schema.SchemaProviderCache;
import dev.stxt.schema.SchemaProviderResources;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.TemplateSchemaProvider;

public class STXTParser extends Parser {
	private final ResourcesLoader resourcesLoader;
	
	public STXTParser (ResourcesLoader loader) {
		this.resourcesLoader = loader;

		List<SchemaProvider> providers = new ArrayList<>();
		providers.add(new SchemaProviderResources(this.resourcesLoader));
		providers.add(new TemplateSchemaProvider(this.resourcesLoader));
		
		SchemaProvider schemaProvider = new SchemaProviderCache(providers);
		SchemaValidator schemaValidator = new SchemaValidator(schemaProvider);
		registerValidator(schemaValidator);
	}
}
