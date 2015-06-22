package feedreader.web.rest.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import common.web.rest.Method;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.Feed;
import feedreader.FeedReader;
import feedreader.Stream;
import feedreader.UserFeedItemContext;
import feedreader.persist.FeedEntityHandler;
import feedreader.web.rest.output.FeedItemResource;
import feedreader.web.rest.output.ResourceHrefBuilder;
import feedreader.web.rest.output.StreamResource;

/**
 * Handler that provides REST-ful services for the {@link Stream}
 * @author jared.pearson
 */
@Singleton
public class StreamResourceHandler implements ResourceHandler {
	private final DataSource dataSource;
	private final FeedEntityHandler feedEntityHandler;
	
	@Inject
	public StreamResourceHandler(DataSource dataSource, FeedEntityHandler feedEntityHandler) {
		this.dataSource = dataSource;
		this.feedEntityHandler = feedEntityHandler;
	}
	
	/**
	 * Gets the aggregate feed, which contains all feed items across multiple RSS feeds. 
	 */
	@RequestHandler(value = "^/v1/stream$", method = Method.GET)
	public StreamResource getStream(HttpServletRequest request, FeedReader feedReader) throws IOException, ServletException, SQLException {
		final Stream stream = feedReader.getStream();
		
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		
		// collect all of the IDs of the feeds referenced by the feed items
		final Set<Integer> feedIdSet = Sets.newHashSetWithExpectedSize(stream.getItems().size());
		for (final UserFeedItemContext feedItem : stream.getItems()) {
			final Integer feedId = feedItem.getFeedId();
			if (feedId == null) {
				continue;
			}
			feedIdSet.add(feedId);
		}
		
		Map<Integer, Feed> loadedFeeds;
		final Connection cnn = dataSource.getConnection();
		try {
			// load all of the feeds associated to the IDs
			loadedFeeds = feedEntityHandler.findFeedsAndFeedItemsByFeedIds(cnn, feedIdSet, 0);
		} finally {
			cnn.close();
		}
		
		// create the response models
		final FeedItemResource[] items = new FeedItemResource[stream.getItems().size()];
		for(int index = 0; index < stream.getItems().size(); index++) {
			final UserFeedItemContext feedItem = stream.getItems().get(index);
			
			final Feed feed = feedItem.getFeedId() == null ? null : loadedFeeds.get(feedItem.getFeedId());
			
			final FeedItemResource feedItemResource = FeedItemResource.fromFeedItem(feedItem, hrefBuilder, feed);
			items[index] = feedItemResource; 
		}
		
		return new StreamResource(items);
	}
	
}
