package feedreader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import common.messagequeue.api.MessageSender;
import feedreader.messagequeue.RetrieveFeedMessageBuilder;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedItemEntityHandler;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.persist.UserFeedItemContextEntityHandler;

/**
 * Main service class for a user to interact with the FeedReader application
 * @author jared.pearson
 */
public class FeedReader {
	private final int MAX_FEED_ITEMS = 25;
	private final DataSource dataSource;
	private final int userId;
	private final MessageSender messageSender;
	private final FeedEntityHandler feedEntityHandler;
	private final FeedItemEntityHandler feedItemEntityHandler;
	private final UserFeedItemContextEntityHandler userFeedItemContextEntityHandler;
	private final FeedRequestEntityHandler feedRequestEntityHandler;
	
	public FeedReader(
			DataSource dataSource, 
			int userId, 
			MessageSender messageSender,
			FeedEntityHandler feedEntityHandler,
			FeedItemEntityHandler feedItemEntityHandler,
			UserFeedItemContextEntityHandler userFeedItemContextEntityHandler,
			FeedRequestEntityHandler feedRequestEntityHandler) {
		this.dataSource = dataSource;
		this.userId = userId;
		this.messageSender = messageSender;
		this.feedEntityHandler = feedEntityHandler;
		this.feedItemEntityHandler = feedItemEntityHandler;
		this.userFeedItemContextEntityHandler = userFeedItemContextEntityHandler;
		this.feedRequestEntityHandler = feedRequestEntityHandler;
	}
	
	/**
	 * Adds a feed from the specified URL.
	 */
	public int addFeedFromUrl(final String url) throws IOException {
		if(url == null) {
			throw new IllegalArgumentException();
		}
		
		try {
			Connection cnn = dataSource.getConnection();
			try {

				//create the request to retrieve the feed
				final int requestId = feedRequestEntityHandler.insert(cnn, url, userId);
				
				//queue up the url to be processed async
				messageSender.send("feedRequest", new RetrieveFeedMessageBuilder(requestId));
				
				return requestId;
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw Throwables.propagate(exc);
		}
	}
	
	/**
	 * Gets a stream for the current user. A stream is a collection of feed items the user is subscribed to.
	 */
	public Stream getStream() {
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				final int offset = 0;
				List<FeedItem> feedItems = feedItemEntityHandler.getFeedItemsForStream(cnn, userId, MAX_FEED_ITEMS, offset);
		

				//get all of the contexts for the user from the feed items
				List<UserFeedItemContext> contexts = getFeedContexts(cnn, feedItems);
				
				//build the final list of feed items from the feed contexts retrieved
				List<UserFeedItemContext> userFeedItems = fanoutFeedItems(feedItems, contexts);
				
				return new Stream(userFeedItems);
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw Throwables.propagate(exc);
		}
	}
	
	/**
	 * Gets the feed with the specified ID for the current user.
	 */
	public UserFeedContext getFeed(int feedId) {
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				final Feed feed = feedEntityHandler.findFeedAndFeedItemsByFeedId(cnn, feedId, MAX_FEED_ITEMS);
				if(feed == null) {
					throw new IllegalArgumentException();
				}
		
				//load the context for the feed items
				final List<UserFeedItemContext> itemContexts = userFeedItemContextEntityHandler.getFeedItemsForUserFeed(cnn, userId, feedId);
				
				//fanout all of the items so the user has a full set of feed items
				final List<UserFeedItemContext> userFeedItems = fanoutFeedItems(feed.getItems(), itemContexts);
				
				//get the user context for the feed
				return new UserFeedContext(feed, userFeedItems);
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw Throwables.propagate(exc);
		}
	}

	/**
	 * Gets the feed item with the specified ID for the current user.
	 */
	public UserFeedItemContext getFeedItem(int feedItemId) {
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				final UserFeedItemContext feedItemContext = userFeedItemContextEntityHandler.getFeedItem(cnn, userId, feedItemId);
				
				if (feedItemContext == null) {
					final FeedItem feedItem = feedItemEntityHandler.getFeedItemById(cnn, feedItemId);
					final UserFeedItemContext context = new UserFeedItemContext();
					context.setFeedItem(feedItem);
					context.setOwnerId(userId);
					return context;
				}
				
				return feedItemContext;
				
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw Throwables.propagate(exc);
		}
	}
	
	/**
	 * Gets a page of FeedRequest instances for the current user. The results should be ordered from newest to oldest. 
	 */
	public @Nonnull FeedRequestPage getFeedRequestsForCurrentUser(final int pageIndex, final int pageSize) {
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				
				final List<FeedRequest> feedRequestPageItems = feedRequestEntityHandler.getFeedRequestsForUser(cnn, userId, pageIndex, pageSize);
				final int total = feedRequestEntityHandler.getTotalNumberOfFeedRequestsForUser(cnn, userId);
				
				return new FeedRequestPage(feedRequestPageItems, total);
				
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw Throwables.propagate(exc);
		}
	}
	
	/**
	 * Marks the feed item corresponding to the specified ID with the specified read status.
	 */
	public void markReadStatus(int feedItemId, boolean readStatus) {
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				
				// check the current value to see if it needs to be created or updated
				final Boolean currentReadStatus = userFeedItemContextEntityHandler.loadReadStatus(cnn, feedItemId, userId);
				
				if (currentReadStatus == null) {
					userFeedItemContextEntityHandler.insert(cnn, feedItemId, userId, readStatus);
				} else if(currentReadStatus != readStatus) {
					userFeedItemContextEntityHandler.updateReadStatus(cnn, feedItemId, userId, readStatus);
				}
				
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw Throwables.propagate(exc);
		}
	}
	
	/**
	 * Gets all of the FeedContext object that correspond to the given feed items.
	 */
	private @Nonnull List<UserFeedItemContext> getFeedContexts(@Nonnull Connection cnn, @Nonnull List<FeedItem> feedItems) throws SQLException {
		final Set<Integer> feedItemIds = Sets.newHashSetWithExpectedSize(feedItems.size());
		for(final FeedItem feedItem : feedItems) {
			feedItemIds.add(feedItem.getId());
		}
		
		return userFeedItemContextEntityHandler.getUserFeedItemsForFeedItems(cnn, userId, feedItemIds); 
	}
	
	/**
	 * Given all of the feed items and the persisted contexts, fanout the feed items so that there is a context for
	 * each feed item. The order of the returned contexts are guaranteed to be the same as the given feed items. 
	 */
	private @Nonnull List<UserFeedItemContext> fanoutFeedItems(@Nonnull List<FeedItem> feedItems, @Nonnull List<UserFeedItemContext> contexts) {

		//map each context by feed item id
		Map<Integer, UserFeedItemContext> contextsByFeedItemId = new Hashtable<Integer, UserFeedItemContext>();
		for(UserFeedItemContext context : contexts) {
			contextsByFeedItemId.put(context.getFeedItemId(), context);
		}
		
		//build the final list of feed items from the feed contexts retrieved
		final ArrayList<UserFeedItemContext> userFeedItems = Lists.newArrayListWithExpectedSize(feedItems.size());
		for(FeedItem feedItem : feedItems) {
			if(contextsByFeedItemId.containsKey(feedItem.getId())) {
				userFeedItems.add(contextsByFeedItemId.get(feedItem.getId()));
			} else {
				UserFeedItemContext context = new UserFeedItemContext();
				context.setFeedItem(feedItem);
				context.setOwnerId(userId);
				userFeedItems.add(context);
			}
		}
		return userFeedItems;
	}
}