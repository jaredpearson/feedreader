package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

public class FeedSubscriptionEntityHandlerTest {
	private DatabaseTestUtils databaseTestUtils;
	
	@Before
	public void setup() {
		this.databaseTestUtils = new DatabaseTestUtils();
	}
	
	@Test
	public void testInsert() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			final int feedId = databaseTestUtils.ensureTestFeed(cnn);
			
			final FeedSubscriptionEntityHandler handler = new FeedSubscriptionEntityHandler();
			final int subscriptionId = handler.insert(cnn, userId, feedId);
			
			assertTrue("Expected a valid subscription ID", subscriptionId > 0);
			assertSubscription(cnn, subscriptionId, userId, feedId);
		} finally {
			cnn.close();
		}
	}
	
	private void assertSubscription(Connection cnn, int subscriptionId, int subscriberId, int feedId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("select subscriber, feedId from feedreader.FeedSubscriptions where id = ?");
		try {
			stmt.setInt(1, subscriptionId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if (rst.next()) {
					
					assertEquals("Subscriber ID in the DB does not match", subscriberId, rst.getInt("subscriber"));
					assertEquals("Feed ID in the DB does not match", feedId, rst.getInt("feedId"));
					
				} else {
					fail("Subscription not found with ID: " + subscriptionId);
				}
			} finally {
				rst.close();
			}
		} finally {
			stmt.close();
		}
	}
	
}
