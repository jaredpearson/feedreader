package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import common.persist.DbUtils;
import feedreader.Feed;

public class FeedEntityHandlerTest {
	private DatabaseTestUtils databaseTestUtils;
	
	@Before
	public void setup() {
		this.databaseTestUtils = new DatabaseTestUtils();
	}
	
	@Test
	public void testFindFeedAndFeedItemsByFeedId() throws SQLException {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int feedId = databaseTestUtils.ensureTestFeed(cnn);
			databaseTestUtils.insertTestFeedItem(cnn, feedId);
			
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
	public void testFindFeedAndFeedItemsByUrl() throws SQLException {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final String url = "http://test.com/test" + (new Random().nextInt()) + ".xml";
			final int feedId = databaseTestUtils.insertTestFeed(cnn, url);
			databaseTestUtils.insertTestFeedItem(cnn, feedId);
			
			final FeedEntityHandler handler = new FeedEntityHandler(new FeedItemEntityHandler());
			final Feed feed = handler.findFeedAndFeedItemsByUrl(cnn, url);
			
			assertNotNull("Expected the feed to be loaded since it exists in the database", feed);
			assertEquals("Expected the feed to coorespond to the ID specified", Integer.valueOf(feedId), feed.getId());
			assertEquals("Expected 1 feed item to be loaded", 1, feed.getItems().size());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsert() throws SQLException {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int testUserId = databaseTestUtils.ensureTestUser(cnn);
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
		final PreparedStatement stmt = cnn.prepareStatement("select url, lastUpdated, title, createdBy from feedreader.Feeds where id = ?");
		try {
			stmt.setInt(1, feedId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if(rst.next()) {
					assertEquals("url in DB was different", url, rst.getString("url"));
					assertEquals("lastUpdated in DB was different", lastUpdated, rst.getTimestamp("lastUpdated"));
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
