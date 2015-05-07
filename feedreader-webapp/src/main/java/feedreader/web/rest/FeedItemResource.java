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
		feedItemResource.id = feedItem.getFeedItem().getId();
		feedItemResource.title = feedItem.getTitle();
		feedItemResource.read = feedItem.isRead();
		feedItemResource.pubDate = feedItem.getFeedItem().getPubDate();
		feedItemResource.guid = feedItem.getFeedItem().getGuid();
		
		final FeedResourceLink feedLink = new FeedResourceLink();
		//TODO: fix this multi-object traversal nastiness
		feedLink.id = feedItem.getFeedItem().getFeed().getId();
		feedLink.title = feedItem.getFeedItem().getFeed().getTitle();
		feedLink.href = hrefBuilder.buildHref("/feed/" + feedLink.id);
		feedItemResource.feed = feedLink;
		
		return feedItemResource;
	}
}