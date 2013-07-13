package feedreader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import common.persist.EntityManager;
import feedreader.fetch.FeedLoader;

/**
 * Main service class for a user to interact with the FeedReader application
 * @author jared.pearson
 */
public class FeedReader {
	private EntityManager entityManager;
	private User user;
	private Provider<FeedLoader> feedLoaderProvider;
	
	public FeedReader(EntityManager entityManager, User user) {
		this.entityManager = entityManager;
		this.user = user;

		this.feedLoaderProvider = new Provider<FeedLoader>() {
			@Override
			public FeedLoader get() {
				return new FeedLoader();
			}
		};
	}
	
	/**
	 * Adds a feed from the specified URL.
	 * TODO: make this execute as a batch job instead of having the user wait
	 */
	public UserFeedContext addFeedFromUrl(String url) throws IOException {
		FeedLoader feedLoader = feedLoaderProvider.get();
		Feed feed = null;
		try {
			feed = feedLoader.loadFromUrl(url);
		} catch (XMLStreamException exc) {
			//FIXME: add a more specific exception
			throw new RuntimeException(exc);
		}
		
		feed.setCreatedBy(user);
		feed.setUrl(url);
		
		//save the feed to the database
		entityManager.persist(feed);
		for(FeedItem feedItem : feed.getItems()) {
			entityManager.persist(feedItem);
		}
		
		UserFeedContext userFeedContext = new UserFeedContext(user, feed, new ArrayList<UserFeedItemContext>(0));
		return userFeedContext;
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