package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import common.persist.DbUtils;
import feedreader.UserFeedItemContext;

public class UserFeedItemContextEntityHandlerTest {
	private DatabaseTestUtils databaseTestUtils;
	
	@Before
	public void setup() {
		this.databaseTestUtils = new DatabaseTestUtils();
	}
	
	@Test
	public void testInsert() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int ownerId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			
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
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			
			final int ownerId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			final int contextId = databaseTestUtils.ensureTestUserFeedItemContext(cnn);
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
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			
			final int ownerId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			final int contextId = databaseTestUtils.ensureTestUserFeedItemContext(cnn);
			final boolean readValue = getReadValue(cnn, contextId);
			
			//update the entity
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final Boolean actualReadValue = handler.loadReadStatus(cnn, feedItemId, ownerId);
			
			assertEquals(readValue, actualReadValue);
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testGetFeedItemWithNoContext() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			
			//get the feed item
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final UserFeedItemContext feedItemContext = handler.getFeedItem(cnn, userId, feedItemId);
			
			assertNull("getFeedItem should return null when no context is found", feedItemContext);
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testGetFeedItemWithContext() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			final int feedItemContextId = databaseTestUtils.insertTestUserFeedItemContext(cnn, feedItemId);
			
			//get the feed item
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final UserFeedItemContext feedItemContext = handler.getFeedItem(cnn, userId, feedItemId);
			
			assertNotNull("getFeedItem should not return null since the context should be found", feedItemContext);
			assertEquals("Expected UserFeedItemContext ID to be the one requested", Integer.valueOf(feedItemContextId), feedItemContext.getId());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testGetFeedItemsForUserFeedWithNoContexts() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedId = databaseTestUtils.ensureTestFeed(cnn);
			
			//get the feed items for the user's feed
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final List<UserFeedItemContext> feedItemContextList = handler.getFeedItemsForUserFeed(cnn, userId, feedId);
			
			assertNotNull("getFeedItemsForUserFeed should never return null", feedItemContextList);
			assertTrue("Expected getFeedItemsForUserFeed to be empty", feedItemContextList.isEmpty());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testGetFeedItemsForUserFeedWithContext() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedId = databaseTestUtils.ensureTestFeed(cnn);
			final int feedItemContextId = databaseTestUtils.ensureTestUserFeedItemContext(cnn);
			
			//get the feed items for the user's feed
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final List<UserFeedItemContext> feedItemContextList = handler.getFeedItemsForUserFeed(cnn, userId, feedId);
			
			assertNotNull("getFeedItemsForUserFeed should never return null", feedItemContextList);
			assertEquals("Expected getFeedItemsForUserFeed to have a context since one is in the DB", 1, feedItemContextList.size());
			assertEquals("Expected the user feed item context to be loaded", new Integer(feedItemContextId), feedItemContextList.get(0).getId());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testGetUserFeedItemsForFeedItemsWithNoContext() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			
			//gets the feed items corresponding to the given feed item ID
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final List<UserFeedItemContext> feedItemContextList = handler.getUserFeedItemsForFeedItems(cnn, userId, Sets.<Integer>newHashSet(feedItemId));
			
			assertNotNull("getUserFeedItemsForFeedItems should never return null", feedItemContextList);
			assertTrue("Expected getUserFeedItemsForFeedItems to be empty", feedItemContextList.isEmpty());
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testGetUserFeedItemsForFeedItemsWithContext() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedItemId = databaseTestUtils.ensureTestFeedItem(cnn);
			final int feedItemContextId = databaseTestUtils.insertTestUserFeedItemContext(cnn, feedItemId);
			
			//update the entity
			final UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			final List<UserFeedItemContext> feedItemContextList = handler.getUserFeedItemsForFeedItems(cnn, userId, Sets.<Integer>newHashSet(feedItemId));
			
			assertNotNull("getUserFeedItemsForFeedItems should never return null", feedItemContextList);
			assertEquals("Expected getUserFeedItemsForFeedItems to have a context since one is in the DB", 1, feedItemContextList.size());
			assertEquals("Expected the user feed item context to be loaded", new Integer(feedItemContextId), feedItemContextList.get(0).getId());
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
