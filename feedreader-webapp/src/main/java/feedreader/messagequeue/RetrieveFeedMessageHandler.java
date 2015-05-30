package feedreader.messagequeue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;

import com.google.common.base.Throwables;

import common.Provider;
import common.messagequeue.api.Message;
import common.messagequeue.api.MessageHandler;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;
import feedreader.fetch.FeedLoader;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedItemEntityHandler;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.persist.FeedSubscriptionEntityHandler;

/**
 * Handler for {@link RetrieveFeedMessageBuilder}
 * @author jared.pearson
 */
public class RetrieveFeedMessageHandler implements MessageHandler {
	private static final Logger logger = Logger.getLogger(RetrieveFeedMessageHandler.class.getName());
	private final Provider<FeedLoader> feedLoaderProvider;
	private final DataSource dataSource;
	private final FeedEntityHandler feedEntityHandler;
	private final FeedItemEntityHandler feedItemEntityHandler;
	private final FeedRequestEntityHandler feedRequestEntityHandler;
	private final FeedSubscriptionEntityHandler subscriptionEntityHandler;
	
	public RetrieveFeedMessageHandler(
			final Provider<FeedLoader> feedLoaderProvider, 
			final DataSource dataSource, 
			final FeedEntityHandler feedEntityHandler,
			final FeedItemEntityHandler feedItemEntityHandler,
			final FeedRequestEntityHandler feedRequestEntityHandler,
			final FeedSubscriptionEntityHandler feedSubscriptionEntityHandler) {
		this.feedLoaderProvider = feedLoaderProvider;
		this.dataSource = dataSource;
		this.feedEntityHandler = feedEntityHandler;
		this.feedItemEntityHandler = feedItemEntityHandler;
		this.feedRequestEntityHandler = feedRequestEntityHandler;
		this.subscriptionEntityHandler = feedSubscriptionEntityHandler;
	}
	
	/**
	 * Handles the message being dequeued
	 */
	@Override
	public void dequeue(final Message message) throws IOException {
		final RetrieveFeedMessageBuilder feedMessage = new RetrieveFeedMessageBuilder(message);
		final int feedRequestId = feedMessage.getFeedRequestId();
		
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				cnn.setAutoCommit(false);
				final Savepoint savepoint = cnn.setSavepoint();
				try {
				
					final FeedRequest feedRequest = feedRequestEntityHandler.findFeedRequestById(cnn, feedRequestId);
					
					//check that the request can be found; this could occur if the request was deleted before the message was processed
					if(feedRequest == null) {
						return;
					}
					
					//check to see if the URL has already been retrieved. if not, then fetch and save it
					final int feedId;
					final Feed matchingFeed = feedEntityHandler.findFeedAndFeedItemsByUrl(cnn, feedRequest.getUrl(), 0);
					if(matchingFeed != null) {
						feedId = matchingFeed.getId();
					} else {
						final Feed newFeed = retrieveFeedFromUrl(feedRequest.getUrl());
						feedId = saveFeedAndFeedItems(cnn, newFeed, feedRequest.getCreatedById());
					}
					
					subscribe(cnn, feedId, feedRequest.getCreatedById());

					//if the feed request has already finished, then no need to set the status again
					if(feedRequest.getStatus() != FeedRequestStatus.FINISHED) {
						finalizeRequest(cnn, feedRequest.getId(), feedId);
					}
			
					cnn.commit();
					
				} catch(Exception exc) {
					cnn.rollback(savepoint);
					throw exc;
				}
			} finally {
				cnn.close();
			}
		} catch(Exception exc) {
			try {
				final Connection cnn = dataSource.getConnection();
				try {
					feedRequestEntityHandler.updateRequestStatus(cnn, feedRequestId, FeedRequestStatus.ERROR);
				} finally {
					cnn.close();
				}
			} catch(SQLException exc2) {
				// log but continue if we can't set the status
				logger.log(Level.WARNING, "Status of request could not be set. Continuing as if this error didn't occur.", exc2);
			}
			
			if (exc instanceof IOException) {
				throw (IOException) exc;
			} else {
				throw Throwables.propagate(exc);
			}
		}
	}
	
	private Feed retrieveFeedFromUrl(final String url) throws IOException {
		//retrieve the feed from the URL given in the request
		final FeedLoader feedLoader = feedLoaderProvider.get();
		Feed feed = null;
		try {
			feed = feedLoader.loadFromUrl(url);
		} catch (XMLStreamException exc) {
			//TODO: update the request with the error
			throw new RuntimeException(exc);
		}
		return feed;
	}
	
	private int saveFeedAndFeedItems(final Connection cnn, final Feed feed, final int userId) throws SQLException {
		final int feedId = feedEntityHandler.insert(cnn, feed.getUrl(), feed.getLastUpdated(), feed.getTitle(), userId);
		for(FeedItem feedItem : feed.getItems()) {
			feedItemEntityHandler.insert(cnn, feedId, feedItem.getTitle(), feedItem.getDescription(), feedItem.getLink(), feedItem.getPubDate(), feedItem.getGuid());
		}
		return feedId;
	}
	
	private void finalizeRequest(final Connection cnn, final int feedRequestId, final int feedId) throws SQLException {
		feedRequestEntityHandler.updateRequestFeedAndStatus(cnn, feedRequestId, feedId, FeedRequestStatus.FINISHED);
	}
	
	private int subscribe(final Connection cnn, final int feedId, final int subscriberId) throws SQLException {
		return subscriptionEntityHandler.insert(cnn, subscriberId, feedId);
	}
}