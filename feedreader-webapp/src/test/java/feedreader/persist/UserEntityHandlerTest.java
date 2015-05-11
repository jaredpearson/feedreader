package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import org.junit.Test;

import common.persist.DbUtils;
import common.persist.EntityManager;
import feedreader.User;

public class UserEntityHandlerTest extends DatabaseTest {
	
	@Test
	public void testPersist() throws Exception {
		Connection cnn = null;
		try {
			cnn = getConnection();
			int recordCountBefore = countUsers(cnn);
			
			EntityManager.QueryContext context = createQueryContext(cnn);
			
			//create the user to be persisted
			User user = new User();
			user.setEmail("test@test.com");
			
			//persist the user
			UserEntityHandler handler = new UserEntityHandler();
			handler.persist(context, user);
			
			assertTrue("Expected the user to have an ID after a persist", user.getId() != null);
			
			int recordCountAfter = countUsers(cnn);
			assertEquals("Expected there to be only one record in the database after the persist", recordCountBefore + 1, recordCountAfter);
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testInsert() throws Exception {
		final Connection cnn = getConnection();
		try {
			int recordCountBefore = countUsers(cnn);
			
			//persist the user
			final UserEntityHandler handler = new UserEntityHandler();
			final int userId = handler.insert(cnn, "test@test" + (new Random().nextInt()) + ".com");
			
			assertTrue("Expected the user to have an ID after a persist", userId > 0);
			
			int recordCountAfter = countUsers(cnn);
			assertEquals("Expected there to be only one record in the database after the persist", recordCountBefore + 1, recordCountAfter);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testGet() throws Exception {
		Connection cnn = null;
		try {
			cnn = getConnection();
			
			//insert a test user into the database
			int testUserId = insertTestUser(cnn);
			
			//get the context
			EntityManager.QueryContext context = createQueryContext(cnn);
			
			//attempt to load the user using the handler
			UserEntityHandler handler = new UserEntityHandler();
			User user = (User)handler.get(context, testUserId);
			
			assertTrue("Expected get to return a valid user account", user != null);
			assertEquals("Expected email to be the one specified in the database", "test@test.com", user.getEmail());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private int countUsers(Connection cnn) throws SQLException {
		return DbUtils.executeAggregate(cnn, "select count(id) from feedreader.Users");
	}
}
