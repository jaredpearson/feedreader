package feedreader;

import java.util.Date;

import common.DateUtils;

/**
 * A user's subscription to a particular feed.
 * @author jared.pearson
 */
public class FeedSubscription {
	private Integer id; 
	private User subscriber;
	private Feed feed;
	private long created = -1;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Feed getFeed() {
		return feed;
	}
	
	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	
	public User getSubscriber() {
		return subscriber;
	}
	
	public void setSubscriber(User subscriber) {
		this.subscriber = subscriber;
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
}
