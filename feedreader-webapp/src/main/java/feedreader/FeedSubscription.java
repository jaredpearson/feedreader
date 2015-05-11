package feedreader;

import java.util.Date;

import common.DateUtils;

/**
 * A user's subscription to a particular feed.
 * @author jared.pearson
 */
public class FeedSubscription {
	private Integer id; 
	private Integer subscriberId;
	private Integer feedId;
	private long created = -1;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getFeedId() {
		return feedId;
	}
	
	public void setFeedId(Integer feedId) {
		this.feedId = feedId;
	}
	
	public Integer getSubscriberId() {
		return subscriberId;
	}
	
	public void setSubscriberId(Integer subscriberId) {
		this.subscriberId = subscriberId;
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
}
