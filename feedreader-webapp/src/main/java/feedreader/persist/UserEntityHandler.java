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
import feedreader.User;

/**
 * Entity handler for {@link User} entities
 * @author jared.pearson
 */
@Singleton
public class UserEntityHandler {
	
	/**
	 * Inserts a new user with the specified email.
	 * @return the ID of the new user
	 */
	public int insert(Connection cnn, String email) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be empty");
		Preconditions.checkArgument(email != null && !email.isEmpty(), "email should not be empty");
		PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.Users (email) values (?) returning id");
		try {
			stmt.setString(1, email);
			
			boolean hasResult = stmt.execute();
			if(hasResult) {
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
				throw new IllegalStateException("Error while inserting user");
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Loads a user corresponding to the given user ID. If no user is found, then a null reference is returned.
	 * @return the user with the specified ID or null if not found
	 * @throws SQLException
	 */
	public @Nullable User loadUserById(@Nonnull Connection cnn, int userId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be empty");
		final PreparedStatement stmt = cnn.prepareStatement("select id, email from feedreader.Users where id = ? limit 1");
		try {
			stmt.setInt(1, userId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				
				if(rst.next()) {
					return createUser(rst);
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
	 * Finds the user with the specified email. If no user has that email then a null reference is returned.
	 * @return the user with the specified email or null if not found
	 */
	public @Nullable User findUserByEmail(@Nonnull Connection cnn, @Nonnull String email) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be empty");
		Preconditions.checkArgument(email != null && !email.isEmpty(), "email should not be empty");
		final PreparedStatement stmt = cnn.prepareStatement("select id, email from feedreader.Users where email = ? limit 1");
		try {
			stmt.setString(1, email);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				
				if(rst.next()) {
					return createUser(rst);
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
	
	private User createUser(ResultSet rst) throws SQLException {
		User user = new User();
		user.setId(rst.getInt("id"));
		user.setEmail(rst.getString("email"));
		return user;
	}
	
}
