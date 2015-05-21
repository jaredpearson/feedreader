package feedreader.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * User session utilities.
 * @author jared.pearson
 */
public class UserSessionUtils {
	private DbUtils dbUtils;
	
	public UserSessionUtils(@Nonnull DbUtils dbUtils) {
		Preconditions.checkArgument(dbUtils != null, "dbUtils should not be null");
		this.dbUtils = dbUtils;
	}

	/**
	 * Creates a new session for the given user. This interacts directly with the database
	 * and does not simulate what a user does to get a new session.
	 * @param userId the ID of the user
	 * @return the ID of the new session
	 */
	public int createUserSession(int userId) throws SQLException {
		final Connection cnn = this.dbUtils.getConnection();
		try {
			return this.createUserSession(cnn, userId);
		} finally {
			cnn.close();
		}
	}

	private int createUserSession(final Connection cnn, int userId)
			throws SQLException {
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
					rst.close();
				}
			} else {
				throw new IllegalStateException("Error while inserting User");
			}
		} finally {
			stmt.close();
		}
	}
}
