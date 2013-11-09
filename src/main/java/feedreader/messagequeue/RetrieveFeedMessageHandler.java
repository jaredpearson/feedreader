package feedreader.messagequeue;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import common.Provider;
import common.messagequeue.Message;
import common.messagequeue.MessageHandler;
import common.persist.EntityManager;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;
import feedreader.fetch.FeedLoader;

/**
 * Handler for {@link RetrieveFeedMessage}
 * @author jared.pearson
 */
public class RetrieveFeedMessageHandler implements MessageHandler {
	private final EntityManager entityManager;
	private final Provider<FeedLoader> feedLoaderProvider;
	
	public RetrieveFeedMessageHandler(final EntityManager entityManager, final Provider<FeedLoader> feedLoaderProvider) {
		this.entityManager = entityManager;
		this.feedLoaderProvider = feedLoaderProvider;
	}
	
	/**
	 * Handles the message being dequeued
	 */
	public void dequeue(final Message message) throws IOException {
		final RetrieveFeedMessage feedMessage = new RetrieveFeedMessage(message);
		final FeedRequest feedRequest = entityManager.get(FeedRequest.class, feedMessage.getFeedRequestId());
		
		//check that the request can be found; this could occur if the request was deleted before the message was processed
		if(feedRequest == null) {
			return;
		}
		
		//if the feed request has already finished, then no need to process it again
		if(feedRequest.getStatus() == FeedRequestStatus.FINISHED) {
			return;
		}
		
		//retrieve the feed from the URL given in the request
		final FeedLoader feedLoader = feedLoaderProvider.get();
		Feed feed = null;
		try {
			feed = feedLoader.loadFromUrl(feedRequest.getUrl());
			feed.setCreatedBy(feedRequest.getCreatedBy());
		} catch (XMLStreamException exc) {
			//TODO: update the request with the error
			throw new RuntimeException(exc);
		}
		
		//save the feed to the database
		entityManager.persist(feed);
		for(FeedItem feedItem : feed.getItems()) {
			entityManager.persist(feedItem);
		}
		
		//update the feed request
		feedRequest.setFeed(feed);
		feedRequest.setStatus(FeedRequestStatus.FINISHED);
		entityManager.persist(feedRequest);
	}
}