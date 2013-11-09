package feedreader;

import java.io.IOException;
import java.util.List;

import common.messagequeue.MessageSender;
import common.persist.EntityManager;
import feedreader.messagequeue.RetrieveFeedMessage;

/**
 * Main service class for a user to interact with the FeedReader application
 * @author jared.pearson
 */
public class FeedReader {
	private final EntityManager entityManager;
	private final User user;
	private final MessageSender messageSender;
	
	public FeedReader(EntityManager entityManager, User user, MessageSender messageSender) {
		this.entityManager = entityManager;
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
		entityManager.persist(feedRequest);
		
		//queue up the url to be processed async
		messageSender.send("feedRequest", new RetrieveFeedMessage(feedRequest));
		
		return feedRequest;
	}
	
	/**
	 * Gets the feed with the specified ID.
	 */
	public UserFeedContext getFeed(int feedId) {
		Feed feed = entityManager.get(Feed.class, feedId);
		if(feed == null) {
			throw new IllegalArgumentException();
		}
		
		int userId = user.getId();
		
		//load the context for the feed items
		List<UserFeedItemContext> itemContexts = entityManager.executeNamedQuery(UserFeedItemContext.class, "getFeedItemsForUserFeed", userId, feedId);
		
		//get the user context for the feed
		UserFeedContext feedContext = new UserFeedContext(user, feed, itemContexts);
		
		return feedContext;
	}
}