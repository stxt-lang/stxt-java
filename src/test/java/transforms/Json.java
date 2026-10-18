package transforms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Reading and writing of JSON text, the same for every transform.
 */
final class Json {
	static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;
	// Decimals as BigDecimal, so that importing 1.10 writes 1.10 and not 1.1
	private static final ObjectMapper MAPPER = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

	private Json() {
	}

	static JsonNode read(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Not a JSON text: " + e.getOriginalMessage(), e);
		}
	}

	// The text of a string, a number or a boolean: numbers with their value, not in exponent form
	static String text(JsonNode scalar) {
		if (scalar.isIntegralNumber())
			return scalar.bigIntegerValue().toString();
		if (scalar.isNumber())
			return scalar.decimalValue().toPlainString();
		return scalar.asText();
	}

	static String write(JsonNode json) {
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}
