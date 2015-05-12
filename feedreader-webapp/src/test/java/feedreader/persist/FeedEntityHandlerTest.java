package feedreader.persist;

import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import common.persist.DbUtils;
import feedreader.Feed;

public class FeedEntityHandlerTest extends DatabaseTest {
	
	@Test
	public void testFindFeedAndFeedItemsByFeedId() throws SQLException {
		final Connection cnn = getConnection();
		try {
			final int feedId = ensureTestFeed(cnn);
			insertTestFeedItem(cnn, feedId);
			
			final FeedEntityHandler handler = new FeedEntityHandler(new FeedItemEntityHandler());
			final Feed feed = handler.findFeedAndFeedItemsByFeedId(cnn, feedId);
			
			assertNotNull("Expected the feed to be loaded since it exists in the database", feed);
			assertEquals("Expected the feed to coorespond to the ID specified", Integer.valueOf(feedId), feed.getId());
			assertEquals("Expected 1 feed item to be loaded", 1, feed.getItems().size());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsert() throws SQLException {
		Connection cnn = getConnection();
		try {
			final int testUserId = ensureTestUser(cnn);
			final String title = "Test Feed";
			final String url = "http://test.com/test.xml";
			final Date lastUpdated = new Date();
			
			FeedEntityHandler handler = new FeedEntityHandler(new FeedItemEntityHandler());
			final int feedId = handler.insert(cnn, url, lastUpdated, title, testUserId);
			
			assertTrue("Expected a valid Feed ID to be specified", feedId > 0);
			assertFeed(cnn, feedId, url, lastUpdated, title, testUserId);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private  void assertFeed(Connection cnn, int feedId, String url, java.util.Date lastUpdated, String title, int createdByUserId) throws SQLException {
		// the last updated value in the database is just MM/dd/yyyy
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastUpdated);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		
		final PreparedStatement stmt = cnn.prepareStatement("select url, lastUpdated, title, createdBy from feedreader.Feeds where id = ?");
		try {
			stmt.setInt(1, feedId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if(rst.next()) {
					assertEquals("url in DB was different", url, rst.getString("url"));
					assertEquals("lastUpdated in DB was different", cal.getTime(), rst.getTimestamp("lastUpdated"));
					assertEquals("title in DB was different", title, rst.getString("title"));
					assertEquals("createdByUserId in DB was different", createdByUserId, rst.getInt("createdBy"));
				} else {
					throw new IllegalStateException("Unable to insert Feed");
				}
				
			} finally {
				DbUtils.close(rst);
			}
			
		} finally {
			DbUtils.close(stmt);
		}
	}
}
