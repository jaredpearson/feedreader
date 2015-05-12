package feedreader.persist;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Preconditions;

import common.persist.DbUtils;
import feedreader.UserSession;

public class UserSessionEntityHandlerTest {
	private DatabaseTestUtils databaseTestUtils;
	
	@Before
	public void setup() {
		this.databaseTestUtils = new DatabaseTestUtils();
	}
	
	@Test
	public void testFindUserSessionById() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			int userId = databaseTestUtils.ensureTestUser(cnn);
			int sessionId = insertUserSession(cnn, userId);
			
			//insert the entity
			final UserSessionEntityHandler handler = new UserSessionEntityHandler();
			final UserSession userSession = handler.findUserSessionById(cnn, sessionId);
			
			assertNotNull("Expected a session to be retrieved", userSession);
			assertEquals("Expected the ID of the loaded session to be the same", Integer.valueOf(sessionId), userSession.getId());
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	@Test
	public void testInsert() throws Exception {
		final Connection cnn = databaseTestUtils.getConnection();
		try {
			final int userId = databaseTestUtils.ensureTestUser(cnn);
			
			//insert the entity
			final UserSessionEntityHandler handler = new UserSessionEntityHandler();
			final int userSessionId = handler.insert(cnn, userId);
			
			assertTrue("Expected the insert to return a valid ID", userSessionId > 0);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	private int insertUserSession(Connection cnn, int userId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.UserSessions (userId) values (?) returning id");
		try {
			stmt.setInt(1, userId);
			
			if(stmt.execute()) {
				final ResultSet rst = stmt.getResultSet();
				try {
					if(rst.next()) {
						return rst.getInt("id");
					} else {
						throw new IllegalStateException("Error while inserting user");
					}
				} finally {
					DbUtils.close(rst);
				}
			} else {
				throw new IllegalStateException("Error while inserting User");
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
}
