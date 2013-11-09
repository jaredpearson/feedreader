package feedreader.messagequeue;

import common.messagequeue.Message;
import common.messagequeue.MessageProducer;
import common.messagequeue.Session;

import feedreader.FeedRequest;

/**
 * Message for when a user requests to retrieve a feed.
 * @author jared.pearson
 */
public class RetrieveFeedMessage implements MessageProducer {
	private static final String FIELD_FEED_REQUEST_ID = "feedRequestId";
	private int feedRequestId;
	
	public RetrieveFeedMessage(final FeedRequest feedRequest) {
		assert feedRequest != null;
		this.feedRequestId = feedRequest.getId();
	}
	
	public RetrieveFeedMessage(final Message message) {
		assert message != null;
		this.feedRequestId = message.getInt(FIELD_FEED_REQUEST_ID);
	}
	
	public int getFeedRequestId() {
		return feedRequestId;
	}
	
	@Override
	public Message toMessage(final Session session) {
		final Message message = session.createMessage(RetrieveFeedMessage.class.getName());
		message.setInt(FIELD_FEED_REQUEST_ID, this.feedRequestId);
		return message;
	}
}