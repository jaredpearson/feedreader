package feedreader.web.rest;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

@Singleton
public class DeserializerUtil {
	private final ObjectMapper jsonObjectMapper;
	
	@Inject
	public DeserializerUtil(ObjectMapper jsonObjectMapper) {
		Preconditions.checkArgument(jsonObjectMapper != null, "jsonObjectMapper should not be null");
		this.jsonObjectMapper = jsonObjectMapper;
	}
	
	/**
	 * Deserializes the body of the request into an instance of the specified class. 
	 */
	public @Nullable <T> T deserializeFromRequestBodyAsJson(@Nonnull HttpServletRequest request, @Nonnull Class<T> clazz) throws IOException {
		assert request != null : "request should not be null";
		assert clazz != null : "clazz should not be null";
		T input;
		final BufferedReader bufferedReader = request.getReader();
		try {
			input = jsonObjectMapper.readValue(bufferedReader, clazz);
		} catch(JsonMappingException exc) {
			throw new InvalidRequestBodyException(exc);
		} catch(JsonParseException exc) {
			throw new InvalidRequestBodyException(exc);
		} catch(EOFException exc) {
			// the body of the request is empty
			input = null;
		} finally {
			bufferedReader.close();
		}
		return input;
	}
	
}
