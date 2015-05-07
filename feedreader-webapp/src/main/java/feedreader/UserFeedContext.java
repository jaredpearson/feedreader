package feedreader;

import java.util.Date;
import java.util.List;

/**
 * An RSS feed for a specific user, which is just a stream containing only one feed. Each user subscribed to a feed will have
 * an instance of UserFeedContext.
 * @author jared.pearson
 */
public class UserFeedContext extends Stream {
	private final Feed feed;
	
	public UserFeedContext(Feed feed, List<UserFeedItemContext> contexts) {
		super(contexts);
		assert feed != null : "feed should not be null";
		this.feed = feed;
	}
	
	public Integer getId() {
		return feed.getId();
	}
	
	public String getTitle() {
		return feed.getTitle();
	}
	
	public Date getCreated() {
		return feed.getCreated();
	}
	
	public User getCreatedBy() {
		return feed.getCreatedBy();
	}
	
	public String getUrl() {
		return feed.getUrl();
	}
	
	public Feed getFeed() {
		return feed;
	}
}
