package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import common.persist.DbUtils;
import feedreader.UserFeedItemContext;

public class UserFeedItemContextEntityHandlerTest extends DatabaseTest {
	
	@Test
	public void testGet() throws SQLException {
		Connection cnn = getConnection();
		try {
			int contextId = ensureTestUserFeedItemContext(cnn);
			
			UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			UserFeedItemContext context = (UserFeedItemContext)handler.get(createQueryContext(cnn), contextId);
			
			assertTrue("Expected a UserFeedItemContext to be retrieved", context != null);
			assertTrue(context.getFeedItemId() != null);
			assertTrue(context.getFeedId() != null);
			
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsert() throws SQLException {
		Connection cnn = getConnection();
		try {
			int ownerId = ensureTestUser(cnn);
			int feedItemId = ensureTestFeedItem(cnn);
			
			//insert the entity
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final int userContextId = handler.insert(cnn, feedItemId, ownerId, false);
			
			assertTrue("Expected the insert to return a valid ID", userContextId > 0);
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testUpdate() throws SQLException {
		Connection cnn = getConnection();
		try {
			
			final int ownerId = ensureTestUser(cnn);
			final int feedItemId = ensureTestFeedItem(cnn);
			final int contextId = ensureTestUserFeedItemContext(cnn);
			final boolean readValueBeforeUpdate = getReadValue(cnn, contextId);
			
			//update the entity
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			handler.updateReadStatus(cnn, feedItemId, ownerId, !readValueBeforeUpdate);
			
			assertEquals(!readValueBeforeUpdate, getReadValue(cnn, contextId));
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testLoadReadStatus() throws Exception {
		Connection cnn = getConnection();
		try {
			
			final int ownerId = ensureTestUser(cnn);
			final int feedItemId = ensureTestFeedItem(cnn);
			final int contextId = ensureTestUserFeedItemContext(cnn);
			final boolean readValue = getReadValue(cnn, contextId);
			
			//update the entity
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final Boolean actualReadValue = handler.loadReadStatus(cnn, feedItemId, ownerId);
			
			assertEquals(readValue, actualReadValue);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private boolean getReadValue(Connection cnn, int contextId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement("select read from feedreader.UserFeedItemContexts where id = ? limit 1");
			stmt.setInt(1, contextId);
			
			ResultSet rst = null;
			try {
				rst = stmt.executeQuery();
				if(rst.next()) {
					return rst.getBoolean("read");
				}
			} finally {
				DbUtils.close(rst);
			}
			
			
		} finally {
			DbUtils.close(stmt);
		}
		throw new IllegalStateException("No UserFeedItemContexts found with ID of " + contextId);
	}
	
}
