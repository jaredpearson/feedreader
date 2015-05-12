package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import common.persist.DbUtils;
import feedreader.User;

public class UserEntityHandlerTest {
	private DatabaseTestUtils databaseTestUtils;
	
	@Before
	public void setup() {
		this.databaseTestUtils = new DatabaseTestUtils();
	}
	
	@Test
	public void testInsert() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
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
	public void testLoadById() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			//insert a test user into the database
			int testUserId = databaseTestUtils.insertTestUser(cnn);
			
			//attempt to load the user using the handler
			final UserEntityHandler handler = new UserEntityHandler();
			final User user = handler.loadUserById(cnn, testUserId);
			
			assertTrue("Expected get to return a valid user account", user != null);
			assertEquals("Expected email to be the one specified in the database", "test@test.com", user.getEmail());
		} finally {
			DbUtils.close(cnn);
		}
	}

	@Test
	public void testLoadByIdWithInvalidId() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			//attempt to load the user using the handler
			final UserEntityHandler handler = new UserEntityHandler();
			final User user = handler.loadUserById(cnn, -1);
			
			assertNull("Expected get to return null since the ID could not be found", user);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private int countUsers(Connection cnn) throws SQLException {
		return DbUtils.executeAggregate(cnn, "select count(id) from feedreader.Users");
	}
}
