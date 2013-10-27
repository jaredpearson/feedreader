package feedreader;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * An RSS feed for a specific user. Each user subscribed to a feed will have
 * an instance of UserFeedContext.
 * @author jared.pearson
 */
public class UserFeedContext {
	private final Feed feed;
	private List<UserFeedItemContext> userFeedItems;
	
	public UserFeedContext(User user, Feed feed, List<UserFeedItemContext> contexts) {
		assert feed != null : "feed should not be null";
		this.feed = feed;
		
		//map each context by feed item id
		Map<Integer, UserFeedItemContext> contextsByFeedItemId = new Hashtable<Integer, UserFeedItemContext>();
		for(UserFeedItemContext context : contexts) {
			contextsByFeedItemId.put(context.getFeedItem().getId(), context);
		}
		
		//get the feed items
		this.userFeedItems = new ArrayList<UserFeedItemContext>();
		for(FeedItem feedItem : feed.getItems()) {
			if(contextsByFeedItemId.containsKey(feedItem.getId())) {
				userFeedItems.add(contextsByFeedItemId.get(feedItem.getId()));
			} else {
				UserFeedItemContext context = new UserFeedItemContext();
				context.setFeedItem(feedItem);
				context.setOwner(user);
				userFeedItems.add(context);
			}
		}
	}
	
	public Integer getId() {
		return feed.getId();
	}
	
	public String getTitle() {
		return feed.getTitle();
	}
	
	public Date getCreated() {
		return feed.getCreated();
	}
	
	public User getCreatedBy() {
		return feed.getCreatedBy();
	}
	
	public String getUrl() {
		return feed.getUrl();
	}
	
	public Feed getFeed() {
		return feed;
	}
	
	/**
	 * Gets the context containing the FeedItem with the specified feed item ID. If
	 * no matches are found, then a <code>null</code> reference is returned.
	 */
	public UserFeedItemContext getItemWithFeedItemId(int feedItemId) {
		for(UserFeedItemContext context : getItems()) {
			if(context.getFeedItem().getId().equals(feedItemId)) {
				return context;
			}
		}
		return null;
	}
	
	public List<UserFeedItemContext> getItems() {
		return userFeedItems;
	}
	
	public boolean isRead(FeedItem feedItem) {
		UserFeedItemContext context = getContextForFeedItem(feedItem);
		if(context == null) {
			return false;
		}
		return context.isRead();
	}
	
	public void markRead(FeedItem feedItem) {
		UserFeedItemContext context = getContextForFeedItem(feedItem);
		if(context == null) {
			context = new UserFeedItemContext();
			context.setFeedItem(feedItem);
			this.userFeedItems.add(context);
		}
		context.setRead(true);
	}
	
	private UserFeedItemContext getContextForFeedItem(FeedItem feedItem) {
		UserFeedItemContext context = null;
		for(UserFeedItemContext thisContext : userFeedItems) {
			if(feedItem.equals(thisContext.getFeedItem())) {
				context = thisContext;
			}
		}
		return context;
	}
}
