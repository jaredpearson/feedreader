package feedreader;

import java.util.Date;

import common.DateUtils;


public class UserFeedItemContext {
	private Integer id;
	private FeedItem feedItem;
	private User owner;
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
		return feedItem.getTitle();
	}
	
	public FeedItem getFeedItem() {
		return feedItem;
	}
	
	public void setFeedItem(FeedItem feedItem) {
		this.feedItem = feedItem;
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

	public User getOwner() {
		return this.owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
}
