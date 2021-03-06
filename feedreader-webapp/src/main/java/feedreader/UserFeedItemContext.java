package feedreader;

import java.util.Date;

import common.DateUtils;

/**
 * An item within an RSS feed for a specific user.
 * @author jared.pearson
 */
public class UserFeedItemContext {
	private Integer id;
	private Integer feedItemId;
	private FeedItem feedItem;
	private Integer ownerId;
	private boolean read = false;
	private long created = -1;
	
	public UserFeedItemContext() {
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTitle() {
		return (feedItem == null) ? null : feedItem.getTitle();
	}
	
	public Integer getFeedId() {
		return (feedItem == null) ? null : feedItem.getFeedId();
	}
	
	public void setFeedItem(FeedItem feedItem) {
		this.feedItem = feedItem;
		this.setFeedItemId(feedItem == null ? null : feedItem.getId());
	}
	
	public Integer getFeedItemId() {
		return feedItemId;
	}
	
	public void setFeedItemId(Integer feedItemId) {
		this.feedItemId = feedItemId;
	}
	
	public boolean isRead() {
		return read;
	}
	
	public void setRead(boolean read) {
		this.read = read;
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}

	public Integer getOwnerId() {
		return this.ownerId;
	}
	
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	
	public Date getPubDate() {
		return (feedItem == null) ? null : feedItem.getPubDate();
	}
	
	public String getGuid() {
		return (feedItem == null) ? null : feedItem.getGuid();
	}
}
