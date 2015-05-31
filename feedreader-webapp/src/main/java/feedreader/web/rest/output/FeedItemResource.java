package feedreader.web.rest.output;

import java.util.Date;

import javax.annotation.Nonnull;

import feedreader.UserFeedItemContext;

/**
 * Resource for the FeedItem entity
 * @author jared.pearson
 */
public class FeedItemResource {
	public int id;
	public String title;
	public boolean read;
	public Date pubDate;
	public String guid;
	public FeedResourceLink feed;
	public Integer feedId;
	
	public static FeedItemResource fromFeedItem(@Nonnull UserFeedItemContext feedItem, @Nonnull ResourceHrefBuilder hrefBuilder) {
		assert feedItem != null : "feedItem should not be null";
		assert hrefBuilder != null : "hrefBuilder should not be null";
		
		final String feedHref = hrefBuilder.buildHref("/feed/" + feedItem.getFeedId());
		final FeedResourceLink feedLink = new FeedResourceLink(feedItem.getFeedId(), null, feedHref);
		
		final FeedItemResource feedItemResource = new FeedItemResource();
		feedItemResource.id = feedItem.getFeedItemId();
		feedItemResource.title = feedItem.getTitle();
		feedItemResource.read = feedItem.isRead();
		feedItemResource.pubDate = feedItem.getPubDate();
		feedItemResource.guid = feedItem.getGuid();
		feedItemResource.feedId = feedItem.getFeedId();
		feedItemResource.feed = feedLink;
		
		return feedItemResource;
	}
}