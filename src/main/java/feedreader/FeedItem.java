package feedreader;

import java.util.Date;

import common.DateUtils;

public class FeedItem {
	private Integer id;
	private Feed feed;
	private String title;
	private String link;
	private String description;
	private String pubDate;
	private String guid;
	private long created = -1;
	
	public FeedItem() {
	}
	
	public FeedItem(String title, String link, String description, String pubDate, String guid) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.pubDate = pubDate;
		this.guid = guid;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	
	public Feed getFeed() {
		return feed;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isUntitled() {
		return title == null;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	
	public String getPubDate() {
		return pubDate;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public String getGuid() {
		return guid;
	}
	
	public void setCreated(Date created) {
		this.created = DateUtils.toMillis(created);
	}
	
	public Date getCreated() {
		return DateUtils.toDate(created);
	}
	
}
