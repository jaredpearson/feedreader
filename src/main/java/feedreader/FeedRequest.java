package feedreader;

import java.util.Date;

import common.DateUtils;

public class FeedRequest {
	private Integer id;
	private String url;
	private Feed feed;
	private String status;
	private long created = -1;
	
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
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
}
