package feedreader.messagequeue;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import common.Provider;
import common.messagequeue.Message;
import common.messagequeue.MessageHandler;
import common.persist.EntityManager;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;
import feedreader.FeedSubscription;
import feedreader.User;
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
		
		try {
			
			//check to see if the URL has already been retrieved. if so, create a subscription for the user
			//otherwise, let's go out and request the feed
			List<Feed> matchingFeeds = entityManager.executeNamedQuery(Feed.class, "findFeedByUrl", feedRequest.getUrl());
			if(!matchingFeeds.isEmpty()) {
				final Feed feed = matchingFeeds.get(0);
				subscribe(feed, feedRequest.getCreatedBy());
				finalizeRequest(feedRequest, feed);
			} else {
				retrieveFeedFromUrl(feedRequest);
			}
		
		} catch(RuntimeException exc) {
			
			feedRequest.setStatus(FeedRequestStatus.ERROR);
			entityManager.persist(feedRequest);
			
			throw exc;
		}
	}
	
	private void retrieveFeedFromUrl(final FeedRequest feedRequest) throws IOException {
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
		finalizeRequest(feedRequest, feed);
		
		//create a subscription for the user to the feed
		subscribe(feed, feedRequest.getCreatedBy());
	}
	
	private void finalizeRequest(final FeedRequest feedRequest, final Feed feed) {
		feedRequest.setFeed(feed);
		feedRequest.setStatus(FeedRequestStatus.FINISHED);
		entityManager.persist(feedRequest);
	}
	
	private FeedSubscription subscribe(final Feed feed, final User subscriber) {
		FeedSubscription subscription = new FeedSubscription();
		subscription.setFeed(feed);
		subscription.setSubscriber(subscriber);
		entityManager.persist(subscription);
		return subscription;
	}
}