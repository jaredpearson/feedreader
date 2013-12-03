package feedreader.web.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import common.web.rest.Method;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;
import feedreader.Stream;
import feedreader.UserFeedItemContext;

/**
 * Handler that provides REST-ful services for the {@link Stream}
 * @author jared.pearson
 */
public class StreamResourceHandler implements ResourceHandler {
	
	/**
	 * Gets the aggregate feed, which contains all feed items across multiple RSS feeds. 
	 */
	@RequestHandler(value = "^/v1/stream$", method = Method.GET)
	public StreamResource getStream(HttpServletRequest request, FeedReader feedReader) throws IOException, ServletException {
		final Stream feed = feedReader.getStream();
		
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		
		//create the response models
		final StreamResource streamResource = new StreamResource();
		streamResource.items = new FeedItemResource[feed.getItems().size()];
		for(int index = 0; index < feed.getItems().size(); index++) {
			final UserFeedItemContext feedItem = feed.getItems().get(index);
			final FeedItemResource feedItemResource = FeedItemResource.fromFeedItem(feedItem, hrefBuilder);
			streamResource.items[index] = feedItemResource; 
		}
		
		return streamResource;
	}
	
	static class StreamResource {
		public FeedItemResource[] items;
	}
	
}
