package feedreader.persist;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import common.persist.DbUtils;
import feedreader.FeedRequest;
import feedreader.User;

public class FeedRequestEntityHandlerTest extends DatabaseTest {

	@Test
	public void testInsert() throws SQLException {
		Connection cnn = null;
		try {
			cnn = getConnection();
			int userId = ensureTestUser(cnn);
			User user = new User();
			user.setId(userId);
			
			FeedRequest feedRequest = new FeedRequest();
			feedRequest.setUrl("http://cyber.law.harvard.edu/rss/examples/rss2sample.xml");
			feedRequest.setCreatedBy(user);
			
			FeedRequestEntityHandler handler = new FeedRequestEntityHandler();
			handler.persist(createQueryContext(cnn), feedRequest);
			
			assertTrue(feedRequest.getId() != null);
			assertTrue(feedRequest.getCreated() != null);
		} finally {
			DbUtils.close(cnn);
		}
	}
}
