package feedreader.web.rest;

import java.util.Date;

import feedreader.UserFeedItemContext;

class FeedItemResource {
	public int id;
	public String title;
	public boolean read;
	public Date pubDate;
	public String guid;
	public FeedResourceLink feed;
	
	public static FeedItemResource fromFeedItem(UserFeedItemContext feedItem, ResourceHrefBuilder hrefBuilder) {
		final FeedItemResource feedItemResource = new FeedItemResource();
		feedItemResource.id = feedItem.getFeedItemId();
		feedItemResource.title = feedItem.getTitle();
		feedItemResource.read = feedItem.isRead();
		feedItemResource.pubDate = feedItem.getPubDate();
		feedItemResource.guid = feedItem.getGuid();
		
		final FeedResourceLink feedLink = new FeedResourceLink();
		feedLink.id = feedItem.getFeedId();
		feedLink.href = hrefBuilder.buildHref("/feed/" + feedLink.id);
		feedItemResource.feed = feedLink;
		
		return feedItemResource;
	}
}