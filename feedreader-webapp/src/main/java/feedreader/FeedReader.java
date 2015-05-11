package feedreader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.google.common.base.Throwables;

import common.messagequeue.api.MessageSender;
import common.persist.EntityManager;
import common.persist.EntityManagerFactory;
import feedreader.messagequeue.RetrieveFeedMessageBuilder;

/**
 * Main service class for a user to interact with the FeedReader application
 * @author jared.pearson
 */
public class FeedReader {
	private static final Logger logger = Logger.getLogger(FeedReader.class.getName()); 
	private final DataSource dataSource;
	private final EntityManagerFactory entityManagerFactory;
	private final User user;
	private final MessageSender messageSender;
	
	public FeedReader(DataSource dataSource, EntityManagerFactory entityManagerFactory, User user, MessageSender messageSender) {
		this.dataSource = dataSource;
		this.entityManagerFactory = entityManagerFactory;
		this.user = user;
		this.messageSender = messageSender;
	}
	
	/**
	 * Adds a feed from the specified URL.
	 */
	public FeedRequest addFeedFromUrl(final String url) throws IOException {
		if(url == null) {
			throw new IllegalArgumentException();
		}
		
		//create the request to retrieve the feed
		final FeedRequest feedRequest = new FeedRequest();
		feedRequest.setUrl(url);
		feedRequest.setCreatedBy(user);
		entityManagerFactory.get().persist(feedRequest);
		
		//queue up the url to be processed async
		messageSender.send("feedRequest", new RetrieveFeedMessageBuilder(feedRequest));
		
		return feedRequest;
	}
	
	/**
	 * Gets a stream for the current user. A stream is a collection of feed items the user is subscribed to.
	 */
	public Stream getStream() {
		final int offset = 0;
		final int size = 25;
		List<FeedItem> feedItems = entityManagerFactory.get().executeNamedQuery(FeedItem.class, "getFeedItemsForStream", user.getId(), size, offset);
		
		//get all of the contexts for the user from the feed items
		List<UserFeedItemContext> contexts = getFeedContexts(feedItems);
		
		//build the final list of feed items from the feed contexts retrieved
		List<UserFeedItemContext> userFeedItems = fanoutFeedItems(feedItems, contexts);
		
		return new Stream(userFeedItems);
	}
	
	/**
	 * Gets the feed with the specified ID for the current user.
	 */
	public UserFeedContext getFeed(int feedId) {
		final EntityManager entityManager = entityManagerFactory.get();
		Feed feed = entityManager.get(Feed.class, feedId);
		if(feed == null) {
			throw new IllegalArgumentException();
		}
		
		int userId = user.getId();
		
		//load the context for the feed items
		List<UserFeedItemContext> itemContexts = entityManager.executeNamedQuery(UserFeedItemContext.class, "getFeedItemsForUserFeed", userId, feedId);
		
		//fanout all of the items so the user has a full set of feed items
		List<UserFeedItemContext> userFeedItems = fanoutFeedItems(feed.getItems(), itemContexts);
		
		//get the user context for the feed
		return new UserFeedContext(feed, userFeedItems);
	}
	
	/**
	 * Marks the feed item corresponding to the specified ID with the specified read status.
	 */
	public void markReadStatus(int feedItemId, boolean readStatus) {
		final int userId = user.getId();
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				
				final PreparedStatement stmt = cnn.prepareStatement("update feedreader.UserFeedItemContexts set read = ? where owner = ? and feedItemdId = ?");
				try {
					stmt.setBoolean(1, readStatus);
					stmt.setInt(2, userId);
					stmt.setInt(3, feedItemId);
					
					final int rowsUpdated = stmt.executeUpdate();
					logger.fine(String.format("Set the read status on %d records", rowsUpdated));
				} finally {
					stmt.close();
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
	private @Nonnull List<UserFeedItemContext> getFeedContexts(@Nonnull List<FeedItem> feedItems) {
		Set<Integer> feedItemIds = new HashSet<Integer>(feedItems.size());
		for(FeedItem feedItem : feedItems) {
			feedItemIds.add(feedItem.getId());
		}
		
		return entityManagerFactory.get().executeNamedQuery(UserFeedItemContext.class, "getUserFeedItemsForFeedItems", user.getId(), feedItemIds); 
	}
	
	/**
	 * Given all of the feed items and the persisted contexts, fanout the feed items so that there is a context for
	 * each feed item. The order of the returned contexts are guaranteed to be the same as the given feed items. 
	 */
	private List<UserFeedItemContext> fanoutFeedItems(List<FeedItem> feedItems, List<UserFeedItemContext> contexts) {

		//map each context by feed item id
		Map<Integer, UserFeedItemContext> contextsByFeedItemId = new Hashtable<Integer, UserFeedItemContext>();
		for(UserFeedItemContext context : contexts) {
			contextsByFeedItemId.put(context.getFeedItem().getId(), context);
		}
		
		//build the final list of feed items from the feed contexts retrieved
		ArrayList<UserFeedItemContext> userFeedItems = new ArrayList<UserFeedItemContext>();
		for(FeedItem feedItem : feedItems) {
			if(contextsByFeedItemId.containsKey(feedItem.getId())) {
				userFeedItems.add(contextsByFeedItemId.get(feedItem.getId()));
			} else {
				UserFeedItemContext context = new UserFeedItemContext();
				context.setFeedItem(feedItem);
				context.setOwner(user);
				userFeedItems.add(context);
			}
		}
		return userFeedItems;
	}
}