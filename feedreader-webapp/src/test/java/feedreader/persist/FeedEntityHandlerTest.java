package feedreader.persist;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import common.persist.DbUtils;
import common.persist.EntityManager;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.User;

public class FeedEntityHandlerTest extends DatabaseTest {
	
	@Test
	public void testGet() throws SQLException {
		Connection cnn = null;
		try {
			cnn = getConnection();
			
			int feedId = ensureTestFeed(cnn);
			
			EntityManager entityManager = mock(EntityManager.class);
			when(entityManager.executeNamedQuery(eq(FeedItem.class), eq("getFeedItemsForFeed"), any())).thenReturn(new ArrayList<FeedItem>());
			
			FeedEntityHandler handler = new FeedEntityHandler();
			Feed feed = (Feed)handler.get(createQueryContext(cnn, entityManager), feedId);
			
			assertTrue(feed != null);
			assertEquals(Integer.valueOf(feedId), feed.getId());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsert() throws SQLException {
		Connection cnn = null;
		try {
			cnn = getConnection();
			
			int testUserId = ensureTestUser(cnn);
			User testUser = new User();
			testUser.setId(testUserId);
			
			Feed feed = new Feed();
			feed.setTitle("Test Feed");
			feed.setUrl("http://test.com/test.xml");
			feed.setCreatedBy(testUser);
			
			FeedEntityHandler handler = new FeedEntityHandler();
			handler.persist(createQueryContext(cnn), feed);
			
			assertTrue(feed.getId() != null);
		} finally {
			DbUtils.close(cnn);
		}
	}
}
