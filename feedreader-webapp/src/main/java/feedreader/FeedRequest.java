package feedreader;

import java.util.Date;

import common.DateUtils;

public class FeedRequest {
	private Integer id;
	private String url;
	private Feed feed;
	private FeedRequestStatus status = FeedRequestStatus.NOT_STARTED;
	private long created = -1;
	private User createdBy;
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	
	public Feed getFeed() {
		return feed;
	}
	
	public void setStatus(FeedRequestStatus status) {
		this.status = status;
	}
	
	public FeedRequestStatus getStatus() {
		return status;
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
	
	public User getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
}
