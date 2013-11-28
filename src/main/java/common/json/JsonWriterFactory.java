package common.json;

import java.io.Writer;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Factory for creating instances of {@link JsonWriter}
 * @author jared.pearson
 * @deprecated
 */
public class JsonWriterFactory {
	private final JsonFactory jacksonJsonFactory;
	
	public JsonWriterFactory() {
		ObjectMapper objectMapper = new ObjectMapper();
		this.jacksonJsonFactory = objectMapper.getJsonFactory();
	}
	
	public JsonWriter createWithWriter(Writer writer) {
		return new JsonWriter(jacksonJsonFactory, writer);
	}
}
