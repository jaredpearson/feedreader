package common.json;

import java.io.IOException;

/**
 * Implementing classes are responsible for writing an object out to JSON using the {@link JsonWriter}
 * @author jared.pearson
 */
public interface JsonMapper<T> {
	public void write(JsonWriter out, T value) throws IOException;
}