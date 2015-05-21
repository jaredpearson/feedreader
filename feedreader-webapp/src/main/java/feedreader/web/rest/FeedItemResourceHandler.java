package feedreader.web.rest;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;
import feedreader.UserFeedItemContext;

@Singleton
public class FeedItemResourceHandler implements ResourceHandler {
	private final ObjectMapper jsonObjectMapper;
	
	@Inject
	public FeedItemResourceHandler(ObjectMapper jsonObjectMapper) {
		Preconditions.checkArgument(jsonObjectMapper != null, "jsonObjectMapper should not be null");
		this.jsonObjectMapper = jsonObjectMapper;
	}

	@RequestHandler(value = "^/v1/feedItem/([0-9]+)$", method = Method.GET) 
	public FeedItemResource getFeedItem(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedItemIdValue) throws IOException {
		final int feedItemId = Integer.valueOf(feedItemIdValue);
		final UserFeedItemContext feedItem = feedReader.getFeedItem(feedItemId);
		if (feedItem == null) {
			response.sendError(404);
			return null;
		}
		
		//output the model as the body of the response
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		return FeedItemResource.fromFeedItem(feedItem, hrefBuilder);
	}
	
	@RequestHandler(value = "^/v1/feedItem/([0-9]+)$", method = Method.PATCH) 
	public FeedItemResource patchFeedItem(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedItemIdValue) throws IOException {
		
		//TODO: we assume that only content of application/json is specified
		
		FeedItemInput input;
		final BufferedReader bufferedReader = request.getReader();
		try {
			input = jsonObjectMapper.readValue(bufferedReader, FeedItemInput.class);
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
		
		if (input == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing input body");
			return null;
		}
		if (input.isReadSet() && input.getRead() == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing read property within request body; must be either \"true\" or \"false\"");
			return null;
		}

		final int feedItemId = Integer.valueOf(feedItemIdValue);
		
		if (input.isReadSet()) {
			feedReader.markReadStatus(feedItemId, input.getRead());
		}

		final UserFeedItemContext feedItem = feedReader.getFeedItem(feedItemId);
		if (feedItem == null) {
			response.sendError(404);
			return null;
		}
		
		//output the model as the body of the response
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		return FeedItemResource.fromFeedItem(feedItem, hrefBuilder);
	}
	
	/**
	 * Input value for updating a Feed Item
	 * @author jared.pearson
	 */
	public static class FeedItemInput {
		private Boolean read = null;
		private boolean isReadSet = false;
		
		public Boolean getRead() {
			return read;
		}
		
		public void setRead(Boolean read) {
			this.read = read;
			this.isReadSet = true;
		}
		
		/**
		 * Determines if the read value was set using the setter. This is useful for 
		 * determining if the value was specified in the request.
		 */
		public boolean isReadSet() {
			return isReadSet;
		}
	}
}
