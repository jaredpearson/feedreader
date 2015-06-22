package feedreader.web.rest.handlers;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;
import feedreader.web.rest.DeserializerUtil;
import feedreader.web.rest.input.FeedItemInputResource;
import feedreader.web.rest.output.FeedItemResource;
import feedreader.web.rest.output.ResourceHrefBuilder;

/**
 * Resource handler for FeedItem entities
 * @author jared.pearson
 */
@Singleton
public class FeedItemResourceHandler implements ResourceHandler {
	private final DeserializerUtil deserializerUtil;
	
	@Inject
	public FeedItemResourceHandler(DeserializerUtil deserializerUtil) {
		Preconditions.checkArgument(deserializerUtil != null, "deserializerUtil should not be null");
		this.deserializerUtil = deserializerUtil;
	}

	@RequestHandler(value = "^/v1/feedItems/([0-9]+)$", method = Method.GET) 
	public FeedItemResource getFeedItem(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedItemIdValue) throws IOException {
		final int feedItemId = Integer.valueOf(feedItemIdValue);
		final UserFeedItemContext feedItem = feedReader.getFeedItem(feedItemId);
		if (feedItem == null) {
			response.sendError(404);
			return null;
		}
		
		final UserFeedContext feed = feedItem.getFeedId() == null ? null : feedReader.getFeed(feedItem.getFeedId());
		
		//output the model as the body of the response
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		return FeedItemResource.fromFeedItem(feedItem, hrefBuilder, feed);
	}
	
	@RequestHandler(value = "^/v1/feedItems/([0-9]+)$", method = Method.PATCH) 
	public FeedItemResource patchFeedItem(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedItemIdValue) throws IOException {
		
		//TODO: we assume that only content of application/json is specified
		final FeedItemInputResource input = deserializerUtil.deserializeFromRequestBodyAsJson(request, FeedItemInputResource.class);
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
		
		final UserFeedContext feed = feedItem.getFeedId() == null ? null : feedReader.getFeed(feedItem.getFeedId());
		
		//output the model as the body of the response
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		return FeedItemResource.fromFeedItem(feedItem, hrefBuilder, feed);
	}
	
}
