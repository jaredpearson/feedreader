package feedreader;

import java.util.Date;

import common.DateUtils;

public class FeedRequest {
	private Integer id;
	private String url;
	private Integer feedId;
	private FeedRequestStatus status = FeedRequestStatus.NOT_STARTED;
	private long created = -1;
	private Integer createdById;
	
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
	
	public void setFeedId(Integer feedId) {
		this.feedId = feedId;
	}

	public Integer getFeedId() {
		return feedId;
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
	
	public Integer getCreatedById() {
		return createdById;
	}
	
	public void setCreatedById(Integer createdById) {
		this.createdById = createdById;
	}

}
