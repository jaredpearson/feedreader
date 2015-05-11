package feedreader.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	public void testInsertWithPersist() throws SQLException {
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
	
	@Test
	public void testInsert() throws SQLException {
		final Connection cnn = getConnection();
		try {
			final int feedId = ensureTestFeed(cnn);
			
			final FeedItemEntityHandler handler = new FeedItemEntityHandler();
			final int feedItemId = handler.insert(cnn, feedId, null, null, null, null, null);
			
			assertTrue("Expected feedItemId to be a valid ID", feedItemId > 0);
			assertFeedItem(cnn, feedItemId, feedId);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private void assertFeedItem(Connection cnn, int feedItemId, int feedId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("select feedId from feedreader.FeedItems where id = ?");
		try {
			stmt.setInt(1, feedItemId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				
				if (rst.next()) {
					
					assertEquals("feedId in DB is not the same", feedId, rst.getInt("feedId"));
					
				} else {
					throw new IllegalStateException();
				}
				
			} finally {
				rst.close();
			}
			
		} finally {
			stmt.close();
		}
	}
}
