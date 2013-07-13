package feedreader;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import common.DateUtils;

public class Feed {
	private Integer id;
	private long created = -1;
	private String url;
	private long lastUpdated = -1;
	private String title;
	private User createdBy;
	private List<FeedItem> items = Collections.emptyList();
	
	public Feed() {
	}
	
	public Feed(String title, List<FeedItem> items) {
		this.title = title;
		this.items = items;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setItems(List<FeedItem> items) {
		this.items = items;
	}
	
	public List<FeedItem> getItems() {
		return items;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = DateUtils.toMillis(lastUpdated);
	}
	
	public Date getLastUpdated() {
		return DateUtils.toDate(lastUpdated);
	}
	
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	public User getCreatedBy() {
		return createdBy;
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
}
