package feedreader.web.rest.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import common.web.rest.Method;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;
import feedreader.Stream;
import feedreader.UserFeedItemContext;
import feedreader.web.rest.output.FeedItemResource;
import feedreader.web.rest.output.ResourceHrefBuilder;
import feedreader.web.rest.output.StreamResource;

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
		final Stream stream = feedReader.getStream();
		
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		
		//create the response models
		final FeedItemResource[] items = new FeedItemResource[stream.getItems().size()];
		for(int index = 0; index < stream.getItems().size(); index++) {
			final UserFeedItemContext feedItem = stream.getItems().get(index);
			final FeedItemResource feedItemResource = FeedItemResource.fromFeedItem(feedItem, hrefBuilder);
			items[index] = feedItemResource; 
		}
		
		return new StreamResource(items);
	}
	
}
