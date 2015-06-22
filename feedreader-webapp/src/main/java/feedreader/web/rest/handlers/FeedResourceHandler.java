package feedreader.web.rest.handlers;

import java.io.IOException;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.Feed;
import feedreader.FeedReader;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;
import feedreader.web.rest.output.FeedItemResource;
import feedreader.web.rest.output.FeedResource;
import feedreader.web.rest.output.ResourceHrefBuilder;

/**
 * Handler that provides REST-ful services for the {@link Feed}
 * @author jared.pearson
 */
@Singleton
public class FeedResourceHandler implements ResourceHandler {
	
	/**
	 * Gets the feed corresponding to the request
	 */
	@RequestHandler(value = "^/v1/feeds/([0-9]+)$", method = Method.GET)
	public FeedResource getFeed(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedIdValue) throws IOException, ServletException {
		
		if (feedIdValue == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter \"id\"");
			return null;
		}
		
		//get the requested feed
		final int feedId;
		try {
			feedId = Integer.valueOf(feedIdValue);
		} catch(NumberFormatException exc) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter \"id\" is not a valid string");
			return null;
		}
		final UserFeedContext feedContext = feedReader.getFeed(feedId);
		
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		
		//create the resource models
		final FeedItemResource[] itemResources = new FeedItemResource[feedContext.getItems().size()];
		for(int index = 0; index < feedContext.getItems().size(); index++) {
			final UserFeedItemContext feedItem = feedContext.getItems().get(index);
			itemResources[index] = FeedItemResource.fromFeedItem(feedItem, hrefBuilder);
		}
		
		return new FeedResource(
				feedContext.getId(),
				feedContext.getTitle(),
				feedContext.getCreated(),
				feedContext.getUrl(),
				itemResources);
	}
	
}