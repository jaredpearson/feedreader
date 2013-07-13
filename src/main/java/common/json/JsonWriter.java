package common.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

/**
 * Simple wrapper that writes JSON to the specified to the writer.
 * <p>
 * TODO: replace with a library like GSON or Jackson
 * @author jared.pearson
 */
public class JsonWriter implements Closeable {
	private enum Type {
		OBJECT,
		ARRAY
	}
	
	private final Writer writer;
	private Stack<Type> typeStack = new Stack<Type>();
	private boolean firstProperty = true;
	private boolean firstArrayElement = true;
	
	public JsonWriter(Writer writer) {
		this.writer = writer;
	}
	
	public JsonWriter startObject() throws IOException {
		writePropertyValue("{", false);
		this.typeStack.push(Type.OBJECT);
		firstProperty = true;
		return this;
	}
	
	public JsonWriter endObject() throws IOException {
		if(this.typeStack.peek() != Type.OBJECT) {
			throw new IllegalStateException();
		}
		this.typeStack.pop();
		writer.write("}");
		
		if(!this.typeStack.isEmpty() && this.typeStack.peek() == Type.OBJECT) {
			firstProperty = false;
		}
		return this;
	}
	
	public JsonWriter name(String value) throws IOException {
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		if(!firstProperty) {
			writer.write(",");
		}
		writeQuotedString(value);
		writeKeyValueSeparator();
		return this;
	}
	
	public JsonWriter nullValue() throws IOException {
		writePropertyValue(null, false);
		return this;
	}
	
	public JsonWriter value(boolean value) throws IOException {
		writePropertyValue((value) ? "true" : "false", false);
		return this;
	}
	
	public JsonWriter value(long value) throws IOException {
		writePropertyValue(Long.toString(value), false);
		return this;
	}
	
	public JsonWriter value(Number value) throws IOException {
		writePropertyValue((value == null) ? null : value.toString(), false);
		return this;
	}
	
	public JsonWriter value(Date value) throws IOException {
		writePropertyValue((value == null) ? null : Long.toString(value.getTime()), false);
		return this;
	}
	
	public JsonWriter value(Calendar value) throws IOException {
		writePropertyValue((value == null) ? null : Long.toString(value.getTimeInMillis()), false);
		return this;
	}
	
	public JsonWriter value(String value) throws IOException {
		writePropertyValue(value, true);
		return this;
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}
	
	private void writeKeyValueSeparator() throws IOException {
		writer.write(":");
	}
	
	private void writePropertyValue(String value, boolean quote) throws IOException {
		//if we are currently in an array, then we need to add the comma separator
		if(this.inArray() && !firstArrayElement) {
			writer.write(",");
		}
			
		if(value == null) {
			writer.write("null");
		} else if(quote) {
			writeQuotedString(value);
		} else {
			writer.write(value);
		}
		firstProperty = false;
		
		if(this.inArray()) {
			firstArrayElement = false;
		}
	}
	
	/**
	 * Writes the value as an encoded string. The value must not be null.
	 */
	private void writeQuotedString(String value) throws IOException {
		writer.write("\"");
		writer.write(value.replaceAll("\"", "\\\""));
		writer.write("\"");
	}

	public void startArray() throws IOException {
		writer.write("[");
		this.typeStack.push(Type.ARRAY);
		this.firstArrayElement = true;
	}
	
	public void endArray() throws IOException {
		if(this.typeStack.peek() != Type.ARRAY) {
			throw new IllegalStateException();
		}
		writer.write("]");
		this.typeStack.pop();

		if(!this.typeStack.isEmpty() && this.typeStack.peek() == Type.ARRAY) {
			firstArrayElement = false;
		}
	}
	
	private boolean inArray() {
		return !this.typeStack.isEmpty() && this.typeStack.peek() == Type.ARRAY;
	}
}