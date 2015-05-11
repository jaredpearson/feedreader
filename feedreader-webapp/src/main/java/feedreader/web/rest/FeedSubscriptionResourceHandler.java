package feedreader.web.rest;

import java.io.BufferedReader;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import common.web.rest.Method;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;

@Singleton
public class FeedSubscriptionResourceHandler implements ResourceHandler {
	private final ObjectMapper jsonObjectMapper;
	
	@Inject
	public FeedSubscriptionResourceHandler(ObjectMapper objectMapper) {
		this.jsonObjectMapper = objectMapper;
	}
	
	@RequestHandler(value="^/v1/feedSubscription$", method=Method.POST)
	public CreateFeedSubscriptionResponse createSubscription(HttpServletRequest request, FeedReader feedReader) 
			throws IOException, ServletException {
		
		//TODO: we assume that only content of application/json is specified
		
		//read the data from the post body
		CreateFeedSubscriptionRequestData input = null;
		BufferedReader bufferedReader = request.getReader();
		try {
			input = jsonObjectMapper.readValue(bufferedReader, CreateFeedSubscriptionRequestData.class);
		} catch(JsonMappingException exc) {
			throw new InvalidRequestBodyException(exc);
		} catch(JsonParseException exc) {
			throw new InvalidRequestBodyException(exc);
		} finally {
			bufferedReader.close();
		}
		
		int feedRequestId = feedReader.addFeedFromUrl(input.url);
		
		//output the success
		CreateFeedSubscriptionResponse responseModel = new CreateFeedSubscriptionResponse();
		responseModel.success = true;
		responseModel.feedRequestId = feedRequestId;
		return responseModel;
	}
	
	public static class CreateFeedSubscriptionRequestData {
		public String url;
	}
	
	public static class CreateFeedSubscriptionResponse {
		public boolean success;
		public int feedRequestId;
	}
	
}
