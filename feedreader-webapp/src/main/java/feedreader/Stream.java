package feedreader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An aggregation of many feed items (ie. feed items not necessarily from the same RSS feed).
 * @author jared.pearson
 */
public class Stream {
	private final List<UserFeedItemContext> userFeedItems;
	private final Map<Integer, UserFeedItemContext> userFeedItemsByFeedItemId;
	
	public Stream(List<UserFeedItemContext> userFeedItems) {
		this.userFeedItems = userFeedItems;
		
		//build a map of user feed items to feed item ID
		this.userFeedItemsByFeedItemId = new HashMap<Integer, UserFeedItemContext>();
		for(UserFeedItemContext context : userFeedItems) {
			Integer feedItemId = context.getFeedItem().getId();
			if(feedItemId == null) {
				continue;
			}
			this.userFeedItemsByFeedItemId.put(feedItemId, context);
		}
	}
	
	/**
	 * Gets the context containing the FeedItem with the specified feed item ID. If
	 * no matches are found, then a <code>null</code> reference is returned.
	 */
	public UserFeedItemContext getItemWithFeedItemId(int feedItemId) {
		return userFeedItemsByFeedItemId.get(feedItemId);
	}
	
	public List<UserFeedItemContext> getItems() {
		return userFeedItems;
	}
	
	/**
	 * Determines if the specified feed item has already been read
	 */
	public boolean isRead(FeedItem feedItem) {
		UserFeedItemContext context = getItemForFeedItem(feedItem);
		if(context == null) {
			return false;
		}
		return context.isRead();
	}
	
	/**
	 * Marks the specified feed item as read. If the item is already read, this
	 * is a no op.
	 */
	public void markRead(FeedItem feedItem) {
		UserFeedItemContext context = getItemForFeedItem(feedItem);
		if(context == null) {
			context = new UserFeedItemContext();
			context.setFeedItem(feedItem);
			this.userFeedItems.add(context);
		}
		context.setRead(true);
	}
	
	private UserFeedItemContext getItemForFeedItem(FeedItem feedItem) {
		if(feedItem == null || feedItem.getId() == null) {
			return null;
		}
		return getItemWithFeedItemId(feedItem.getId());
	}
}
