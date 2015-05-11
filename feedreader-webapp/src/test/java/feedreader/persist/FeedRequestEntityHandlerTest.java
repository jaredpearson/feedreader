package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import common.persist.DbUtils;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;

public class FeedRequestEntityHandlerTest extends DatabaseTest {
	
	@Test
	public void testFindFeedRequestById() throws SQLException {
		Connection cnn = getConnection();
		try {
			final String url = "http://cyber.law.harvard.edu/rss/examples/rss2sample.xml";
			final int createdByUserId = ensureTestUser(cnn);
			final int requestId = insertFeedRequest(cnn, url, createdByUserId);
			
			final FeedRequestEntityHandler handler = new FeedRequestEntityHandler();
			final FeedRequest loadedRequest = handler.findFeedRequestById(cnn, requestId);
			
			assertNotNull("Expected findFeedByRequest to not return null", loadedRequest);
			final String actualStatus = loadFeedRequestStatus(cnn, requestId);
			assertEquals("Expected the status to be be the default.", FeedRequestStatus.NOT_STARTED.getDbValue(), actualStatus);
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testUpdateRequestStatus() throws SQLException {
		Connection cnn = getConnection();
		try {
			final String url = "http://cyber.law.harvard.edu/rss/examples/rss2sample.xml";
			final int createdByUserId = ensureTestUser(cnn);
			final int requestId = insertFeedRequest(cnn, url, createdByUserId);
			
			final FeedRequestEntityHandler handler = new FeedRequestEntityHandler();
			final boolean result = handler.updateRequestStatus(cnn, requestId, FeedRequestStatus.FINISHED);
			
			assertTrue("Expected result to be true", result);
			final String actualStatus = loadFeedRequestStatus(cnn, requestId);
			assertEquals("Expected the status to be updated.", FeedRequestStatus.FINISHED.getDbValue(), actualStatus);
		} finally {
			DbUtils.close(cnn);
		}
	}

	
	@Test
	public void testUpdateRequestFeedAndStatus() throws SQLException {
		Connection cnn = getConnection();
		try {
			final String url = "http://cyber.law.harvard.edu/rss/examples/rss2sample.xml";
			final int createdByUserId = ensureTestUser(cnn);
			final int requestId = insertFeedRequest(cnn, url, createdByUserId);
			final int feedId = ensureTestFeed(cnn);
			
			final FeedRequestEntityHandler handler = new FeedRequestEntityHandler();
			final boolean result = handler.updateRequestFeedAndStatus(cnn, requestId, feedId, FeedRequestStatus.FINISHED);
			
			assertTrue("Expected result to be true", result);
			final String actualStatus = loadFeedRequestStatus(cnn, requestId);
			assertEquals("Expected the status to be updated.", FeedRequestStatus.FINISHED.getDbValue(), actualStatus);
			final int actualFeedId = loadFeedRequestFeedId(cnn, requestId);
			assertEquals("Expected the feed ID to be updated.", feedId, actualFeedId);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private int insertFeedRequest(Connection cnn, String url, int createdByUserId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.FeedRequests (url, createdBy, status) values (?, ?, ?) returning id");
		try {
			stmt.setString(1, url);
			stmt.setInt(2, createdByUserId);
			stmt.setString(3, FeedRequestStatus.NOT_STARTED.getDbValue());
			
			if (stmt.execute()) {
				final ResultSet rst = stmt.getResultSet();
				if (rst.next()) {
					return rst.getInt("id");
				} else {
					throw new IllegalStateException("Unable to insert FeedRequest");
				}
			} else {
				throw new IllegalStateException("Unable to insert FeedRequest");
			}
		} finally {
			DbUtils.close(stmt);
		}
	}

	private String loadFeedRequestStatus(Connection cnn, int requestId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("select status from feedreader.FeedRequests where id = ?");
		try {
			stmt.setInt(1, requestId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if (rst.next()) {
					return rst.getString("status");
				} else {
					throw new IllegalStateException("Unable to load FeedRequest");
				}
			} finally {
				rst.close();
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	private int loadFeedRequestFeedId(Connection cnn, int requestId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("select feedId from feedreader.FeedRequests where id = ?");
		try {
			stmt.setInt(1, requestId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if (rst.next()) {
					return rst.getInt("feedId");
				} else {
					throw new IllegalStateException("Unable to load FeedRequest");
				}
			} finally {
				rst.close();
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
}
