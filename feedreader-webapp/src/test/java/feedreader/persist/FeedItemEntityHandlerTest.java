package feedreader.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import common.persist.DbUtils;
import common.persist.EntityManager;
import feedreader.Feed;
import feedreader.FeedItem;

public class FeedItemEntityHandlerTest extends DatabaseTest {

	@Test
	public void testGet() throws SQLException {
		Connection cnn = null;
		try {
			cnn = getConnection();
			
			int feedItemId = ensureTestFeedItem(cnn);
			
			EntityManager entityManager = mock(EntityManager.class);
			when(entityManager.executeNamedQuery(eq(FeedItem.class), eq("getFeedItemsForFeed"), any())).thenReturn(new ArrayList<FeedItem>());
			
			FeedItemEntityHandler handler = new FeedItemEntityHandler();
			FeedItem feedItem = (FeedItem)handler.get(createQueryContext(cnn, entityManager), feedItemId);
			
			assertTrue(feedItem != null);
			assertEquals(Integer.valueOf(feedItemId), feedItem.getId());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsert() throws SQLException {
		Connection cnn = null;
		try {
			cnn = getConnection();
			
			int feedId = ensureTestFeed(cnn);
			Feed feed = new Feed();
			feed.setId(feedId);
			
			FeedItem feedItem = new FeedItem();
			feedItem.setFeed(feed);
			
			FeedItemEntityHandler handler = new FeedItemEntityHandler();
			handler.persist(createQueryContext(cnn), feedItem);
			
			assertTrue(feedItem.getId() != null);
		} finally {
			DbUtils.close(cnn);
		}
	}
}
