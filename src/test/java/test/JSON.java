package test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.TextNode;
import dev.stxt.schema.ChildDefinition;
import dev.stxt.schema.NodeDefinition;
import dev.stxt.schema.Schema;

public final class JSON {
	private static final String IDENTER_STRING = "    "; // It could also be "\t"

	// Mixin: 'description' is a new, optional field in NodeDefinition; it is left out when it is
	// null so the existing fixtures, which do not declare it, keep working. The other fields
	// (e.g. min/max in ChildDefinition) still serialize null explicitly, as they always did.
	private abstract static class NodeDefinitionMixin {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		abstract String getDescription();

		// The fixtures predate 1.0 and keep the historical 'normalized_name' member for the
		// canonical name (the API was renamed getCanonicalName() in 0.7.0, alias removed in 0.11.0).
		@JsonProperty("normalized_name")
		abstract String getCanonicalName();
	}

	// Same for the description of the Schema itself (0.11.0): left out when null.
	private abstract static class SchemaMixin {
		@JsonInclude(JsonInclude.Include.NON_NULL)
		abstract String getDescription();
	}

	private abstract static class ChildDefinitionMixin {
		@JsonProperty("normalized_name")
		abstract String getCanonicalName();
	}

	/**
	 * Serializes a {@link Node} in the historical shape of the {@code docs_json/} fixtures, which
	 * predate the 0.7.0 node model: the same members for both forms (an empty {@code value} for
	 * text nodes, an empty {@code text_lines} for inline ones), no {@code parent} (which would be a
	 * cycle) and no members added since. Bean serialization of the current classes would produce a
	 * different, cyclic shape; this keeps the fixtures valid as regression data. The canonical tree
	 * of STXT-TREE-SPEC is a different, normative serialization ({@code dev.stxt.runtime.TreeJson}).
	 */
	private static final class LegacyNodeSerializer extends ValueSerializer<Node> {
		@Override
		public void serialize(Node node, JsonGenerator gen, SerializationContext context) {
			gen.writeStartObject();
			gen.writeStringProperty("name", node.getName());
			gen.writeStringProperty("normalized_name", node.getCanonicalName());
			gen.writeStringProperty("namespace", node.getNamespace());
			gen.writeBooleanProperty("text_node", node.isTextNode());
			gen.writeStringProperty("value", node instanceof InlineNode inline ? inline.getValue() : "");
			gen.writeArrayPropertyStart("text_lines");
			if (node instanceof TextNode text)
				for (String line : text.getTextLines())
					gen.writeString(line);
			gen.writeEndArray();
			gen.writeNumberProperty("line", node.getLine());
			gen.writeNumberProperty("level", node.getLevel());
			gen.writeArrayPropertyStart("children");
			if (node instanceof InlineNode inline)
				for (Node child : inline.getChildren())
					serialize(child, gen, context);
			gen.writeEndArray();
			gen.writeStringProperty("qualified_name", node.getQualifiedName());
			gen.writeStringProperty("text", node.getText());
			gen.writeEndObject();
		}
	}

	// Shared pretty-print configuration (tab indentation)
	private static final DefaultIndenter INDENTER = new DefaultIndenter(IDENTER_STRING, DefaultIndenter.SYS_LF);
	private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter().withObjectIndenter(INDENTER)
			.withArrayIndenter(INDENTER);

	private static JsonMapper.Builder builder() {
		return JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.addMixIn(NodeDefinition.class, NodeDefinitionMixin.class)
				.addMixIn(ChildDefinition.class, ChildDefinitionMixin.class)
				.addMixIn(Schema.class, SchemaMixin.class)
				.addModule(new SimpleModule().addSerializer(Node.class, new LegacyNodeSerializer()));
	}

	private static final JsonMapper MAPPER = builder().build();

	private static final JsonMapper MAPPER_PRETTY = builder()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.defaultPrettyPrinter(PRETTY_PRINTER)
			.build();

	private JSON() {
		// prevent instantiation
	}

	public static final String toJson(Object obj) {
		return MAPPER.writeValueAsString(obj);
	}

	public static final String toJsonPretty(Object obj) {
		return MAPPER_PRETTY.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}

	public static JsonNode toJsonTree(Object obj) {
		return toJsonTree(toJson(obj));
	}

	public static JsonNode toJsonTree(String json) {
		return MAPPER.readTree(json);
	}
}
