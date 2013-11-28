package feedreader.web.rest;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import common.web.rest.Method;
import common.web.rest.RequestHandler;
import feedreader.FeedReader;
import feedreader.FeedRequest;

public class FeedSubscriptionResourceHandler {
	
	@RequestHandler(value="^/v1/feedSubscription$", method=Method.POST)
	public void createSubscription(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader) 
			throws IOException, ServletException {
		
		//TODO: we assume that only content of application/json is specified
		
		//read the data from the post body
		CreateFeedSubscriptionRequestData input = null;
		BufferedReader bufferedReader = request.getReader();
		try {
			input = new ObjectMapper().readValue(bufferedReader, CreateFeedSubscriptionRequestData.class);
		} catch(JsonMappingException exc) {
			throw new InvalidRequestBodyException(exc);
		} catch(JsonParseException exc) {
			throw new InvalidRequestBodyException(exc);
		} finally {
			bufferedReader.close();
		}
		
		FeedRequest feedRequest = feedReader.addFeedFromUrl(input.url);
		
		//output the success
		response.setContentType("application/json");
		CreateFeedSubscriptionResponse responseModel = new CreateFeedSubscriptionResponse();
		responseModel.success = true;
		responseModel.feedRequestId = feedRequest.getId();
		new ObjectMapper().writeValue(response.getOutputStream(), responseModel);
	}
	
	public static class CreateFeedSubscriptionRequestData {
		public String url;
	}
	
	public static class CreateFeedSubscriptionResponse {
		public boolean success;
		public int feedRequestId;
	}
	
}
