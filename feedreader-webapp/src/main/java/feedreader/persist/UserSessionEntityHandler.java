package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import common.persist.DbUtils;
import feedreader.UserSession;

@Singleton
public class UserSessionEntityHandler {
	
	/**
	 * Gets the user session with the given session ID. If no session exists, then a null reference is returned.
	 * @return the session with the session ID or null if not found
	 */
	public @Nullable UserSession findUserSessionById(@Nonnull Connection cnn, int sessionId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		
		final PreparedStatement stmt = cnn.prepareStatement("select s.id sessionId, s.created, s.userId from feedreader.UserSessions s where s.id = ? limit 1");
		try {
			stmt.setInt(1, sessionId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				
				if (rst.next()) {
					final UserSession session = new UserSession();
					session.setId(rst.getInt("sessionId"));
					session.setCreated(rst.getDate("created"));
					session.setUserId(rst.getInt("userId"));
					return session;
				} else {
					return null;
				}
				
			} finally {
				DbUtils.close(rst);
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Inserts a new user session into the database for the given user.
	 * @return the ID of the new session
	 */
	public int insert(Connection cnn, int userId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.UserSessions (userId) values (?) returning id");
		try {
			stmt.setInt(1, userId);
			
			if(stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					
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
