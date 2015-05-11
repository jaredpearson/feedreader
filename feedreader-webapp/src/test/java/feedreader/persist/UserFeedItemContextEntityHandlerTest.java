package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import common.persist.DbUtils;
import feedreader.FeedItem;
import feedreader.User;
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
			assertTrue(context.getFeedItem() != null);
			assertTrue(context.getFeedItem().getFeed() != null);
			
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsertWithPersist() throws SQLException {
		Connection cnn = getConnection();
		try {
			int ownerId = ensureTestUser(cnn);
			User owner = new User();
			owner.setId(ownerId);
		
			int feedItemId = ensureTestFeedItem(cnn);
			FeedItem feedItem = new FeedItem();
			feedItem.setId(feedItemId);
			
			UserFeedItemContext context = new UserFeedItemContext();
			context.setFeedItem(feedItem);
			context.setOwner(owner);
			context.setRead(false);
			
			//persist the entity
			UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			handler.persist(createQueryContext(cnn), context);
			
			assertTrue("Expected the ID to be set when the entity is persisted", context.getId() != null);
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
	public void testUpdateWithPersist() throws SQLException {
		Connection cnn = getConnection();
		try {
			
			int ownerId = ensureTestUser(cnn);
			User owner = new User();
			owner.setId(ownerId);
		
			int feedItemId = ensureTestFeedItem(cnn);
			FeedItem feedItem = new FeedItem();
			feedItem.setId(feedItemId);
			
			int contextId = ensureTestUserFeedItemContext(cnn);
			boolean readValueBeforeUpdate = getReadValue(cnn, contextId);
			
			UserFeedItemContext context = new UserFeedItemContext();
			context.setId(contextId);
			context.setFeedItem(feedItem);
			context.setOwner(owner);
			context.setRead(!readValueBeforeUpdate);
			
			//persist the entity
			UserFeedItemContextEntityHandler handler = new UserFeedItemContextEntityHandler();
			handler.persist(createQueryContext(cnn), context);
			
			assertTrue("Expected the ID to be set when the entity is persisted", context.getId() != null);
			assertEquals(!readValueBeforeUpdate, getReadValue(cnn, contextId));
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
