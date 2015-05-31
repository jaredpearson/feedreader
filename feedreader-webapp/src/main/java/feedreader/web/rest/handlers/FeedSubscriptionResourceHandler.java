package feedreader.web.rest.handlers;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;

import common.web.rest.Method;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;
import feedreader.web.rest.DeserializerUtil;
import feedreader.web.rest.input.FeedSubscriptionInputResource;

/**
 * Resource handler for FeedSubscriptions
 * @author jared.pearson
 */
@Singleton
public class FeedSubscriptionResourceHandler implements ResourceHandler {
	private final DeserializerUtil deserializerUtil;
	
	@Inject
	public FeedSubscriptionResourceHandler(DeserializerUtil deserializerUtil) {
		Preconditions.checkArgument(deserializerUtil != null, "deserializerUtil should not be null");
		this.deserializerUtil = deserializerUtil;
	}
	
	@RequestHandler(value="^/v1/feedSubscriptions$", method=Method.POST)
	public CreateFeedSubscriptionResource createSubscription(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader) 
			throws IOException, ServletException {
		
		//TODO: we assume that only content of application/json is specified
		final FeedSubscriptionInputResource input = deserializerUtil.deserializeFromRequestBodyAsJson(request, FeedSubscriptionInputResource.class);
		if (input == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing input body");
			return null;
		}
		
		final int feedRequestId = feedReader.addFeedFromUrl(input.url);
		
		//output the success
		return new CreateFeedSubscriptionResource(true, feedRequestId);
	}
	
	/**
	 * Resource returned back to the client when a feed subscription is created.
	 * @author jared.pearson
	 */
	public static class CreateFeedSubscriptionResource {
		public final boolean success;
		public final int feedRequestId;
		
		public CreateFeedSubscriptionResource(boolean success, int feedRequestId) {
			this.success = success;
			this.feedRequestId = feedRequestId;
		}
	}
	
}
