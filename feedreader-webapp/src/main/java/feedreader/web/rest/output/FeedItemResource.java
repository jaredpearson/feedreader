package feedreader.web.rest.output;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import feedreader.Feed;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;

/**
 * Resource for the FeedItem entity
 * @author jared.pearson
 */
public class FeedItemResource {
	public final int id;
	public final String title;
	public final boolean read;
	public final Date pubDate;
	public final String guid;
	public final FeedResourceLink feed;
	public final Integer feedId;
	
	public FeedItemResource(
			final int id,
			final String title,
			final boolean read,
			final Date pubDate,
			final String guid,
			final FeedResourceLink feed,
			final Integer feedId) {
		this.id = id;
		this.title = title;
		this.read = read;
		this.pubDate = pubDate;
		this.guid = guid;
		this.feed = feed;
		this.feedId = feedId;
	}
	
	public static FeedItemResource fromFeedItem(@Nonnull UserFeedItemContext feedItem, @Nonnull ResourceHrefBuilder hrefBuilder, @Nullable UserFeedContext feed) {
		return fromFeedItem(feedItem, hrefBuilder, (feed == null) ? null : feed.getTitle());
	}
	
	public static FeedItemResource fromFeedItem(@Nonnull UserFeedItemContext feedItem, @Nonnull ResourceHrefBuilder hrefBuilder, @Nullable Feed feed) {
		return fromFeedItem(feedItem, hrefBuilder, (feed == null) ? null : feed.getTitle());
	}
	
	private static FeedItemResource fromFeedItem(@Nonnull UserFeedItemContext feedItem, @Nonnull ResourceHrefBuilder hrefBuilder, @Nullable String feedTitle) {
		assert feedItem != null : "feedItem should not be null";
		assert hrefBuilder != null : "hrefBuilder should not be null";
		
		final String feedHref = hrefBuilder.buildHref("/feeds/" + feedItem.getFeedId());
		final FeedResourceLink feedLink = new FeedResourceLink(feedItem.getFeedId(), feedTitle, feedHref);
		
		return new FeedItemResource(
				feedItem.getFeedItemId(),
				feedItem.getTitle(),
				feedItem.isRead(),
				feedItem.getPubDate(),
				feedItem.getGuid(),
				feedLink,
				feedItem.getFeedId());
	}
}