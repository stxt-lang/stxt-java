package test;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;

import dev.stxt.schema.NodeDefinition;

public final class JSON {
	private static final String IDENTER_STRING = "    "; // También podría ser "\t"

	// Mixin: 'description' es un campo nuevo y opcional en NodeDefinition; se omite cuando es
	// null para no romper los fixtures existentes, que no lo declaran. El resto de campos (p.
	// ej. min/max en ChildDefinition) siguen serializando null explícitamente como hasta ahora.
	private abstract static class NodeDefinitionMixin {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		abstract String getDescription();
	}

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.addMixIn(NodeDefinition.class, NodeDefinitionMixin.class);

	// Shared pretty-print configuration (tab indentation)
	private static final DefaultIndenter INDENTER = new DefaultIndenter(IDENTER_STRING, DefaultIndenter.SYS_LF);
	private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter().withObjectIndenter(INDENTER)
			.withArrayIndenter(INDENTER);

	private static final ObjectMapper MAPPER_PRETTY = createPrettyMapper();

	private static ObjectMapper createPrettyMapper() {
		ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.addMixIn(NodeDefinition.class, NodeDefinitionMixin.class)
				.enable(SerializationFeature.INDENT_OUTPUT);

		// Use shared pretty printer
		mapper.setDefaultPrettyPrinter(PRETTY_PRINTER);
		return mapper;
	}

	private JSON() {
		// prevent instantiation
	}

	public static final String toJson(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static final String toJsonPretty(Object obj) {
		try {
			return MAPPER_PRETTY.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode toJsonTree(Object obj) {
		return toJsonTree(toJson(obj));
	}
	
	
	public static JsonNode toJsonTree(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (IOException e) {
			throw new RuntimeException("JSON parsing error", e);
		}
	}
}