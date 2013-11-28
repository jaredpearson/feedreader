package common.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Simple wrapper that writes JSON to the specified to the writer.
 * @author jared.pearson
 * @deprecated Use Jackson {@link ObjectMapper} or {@link JsonGenerator} instead of this class
 */
public class JsonWriter implements Closeable {
	
	private final JsonGenerator jsonGenerator;
	
	public JsonWriter(JsonFactory jsonFactory, Writer writer) {
		try {
			this.jsonGenerator = jsonFactory.createJsonGenerator(writer);
		} catch(IOException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	public JsonWriter startObject() throws IOException {
		jsonGenerator.writeStartObject();
		return this;
	}
	
	public JsonWriter endObject() throws IOException {
		jsonGenerator.writeEndObject();
		return this;
	}
	
	public JsonWriter name(String value) throws IOException {
		jsonGenerator.writeFieldName(value);
		return this;
	}
	
	public JsonWriter nullValue() throws IOException {
		jsonGenerator.writeNull();
		return this;
	}
	
	public JsonWriter value(boolean value) throws IOException {
		jsonGenerator.writeBoolean(value);
		return this;
	}
	
	public JsonWriter value(long value) throws IOException {
		jsonGenerator.writeNumber(value);
		return this;
	}
	
	public JsonWriter value(Number value) throws IOException {
		jsonGenerator.writeObject(value);
		return this;
	}
	
	public JsonWriter value(Date value) throws IOException {
		jsonGenerator.writeObject(value);
		return this;
	}
	
	public JsonWriter value(Calendar value) throws IOException {
		jsonGenerator.writeObject(value);
		return this;
	}
	
	public JsonWriter value(String value) throws IOException {
		jsonGenerator.writeString(value);
		return this;
	}
	
	@Override
	public void close() throws IOException {
		jsonGenerator.close();
	}
	
	public void startArray() throws IOException {
		jsonGenerator.writeStartArray();
	}
	
	public void endArray() throws IOException {
		jsonGenerator.writeEndArray();
	}
}