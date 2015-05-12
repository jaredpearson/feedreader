package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import common.persist.DbUtils;
import feedreader.FeedItem;

public class FeedItemEntityHandlerTest extends DatabaseTest {

	@Test
	public void testGetFeedItemsForFeed() throws Exception {
		final Connection cnn = getConnection();
		try {
			final int feedId = ensureTestFeed(cnn);
			final int feedItemId = insertTestFeedItem(cnn, feedId);
			
			final FeedItemEntityHandler handler = new FeedItemEntityHandler();
			final List<FeedItem> feedItems = handler.getFeedItemsForFeed(cnn, feedId);
			
			assertNotNull("Expected getFeedItemsForFeed to return a value", feedItems);
			assertEquals("Expected only the feed item created within this test to be returned", 1, feedItems.size());
			assertEquals("Expected the feed item to be returned", Integer.valueOf(feedItemId), feedItems.get(0).getId());
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
