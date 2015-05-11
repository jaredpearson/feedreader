package feedreader.messagequeue;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import common.messagequeue.api.Message;
import common.messagequeue.api.MessageBuilder;
import common.messagequeue.api.Session;
import feedreader.FeedRequest;

/**
 * Message for when a user requests to retrieve a feed.
 * @author jared.pearson
 */
public class RetrieveFeedMessageBuilder implements MessageBuilder {
	private static final String FIELD_FEED_REQUEST_ID = "feedRequestId";
	private int feedRequestId;
	
	public RetrieveFeedMessageBuilder(final int feedRequestId) {
		this.feedRequestId = feedRequestId;
	}
	
	public RetrieveFeedMessageBuilder(@Nonnull final FeedRequest feedRequest) {
		Preconditions.checkArgument(feedRequest != null, "feedRequest should not be null");
		this.feedRequestId = feedRequest.getId();
	}
	
	public RetrieveFeedMessageBuilder(final Message message) {
		Preconditions.checkArgument(message != null, "message should not be null");
		this.feedRequestId = message.getInt(FIELD_FEED_REQUEST_ID);
	}
	
	public int getFeedRequestId() {
		return feedRequestId;
	}
	
	@Override
	public Message build(@Nonnull final Session session) {
		Preconditions.checkArgument(session != null, "session should not be null");
		final Message message = session.createMessage(RetrieveFeedMessageBuilder.class.getName());
		message.setInt(FIELD_FEED_REQUEST_ID, this.feedRequestId);
		return message;
	}
}