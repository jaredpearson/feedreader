package feedreader.persist;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.junit.Test;

import common.persist.DbUtils;

public class UserSessionEntityHandlerTest extends DatabaseTest {
	
	@Test
	public void testInsert() throws Exception {
		Connection cnn = getConnection();
		try {
			int userId = ensureTestUser(cnn);
			
			//insert the entity
			final UserSessionEntityHandler handler = new UserSessionEntityHandler();
			final int userSessionId = handler.insert(cnn, userId);
			
			assertTrue("Expected the insert to return a valid ID", userSessionId > 0);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
}
