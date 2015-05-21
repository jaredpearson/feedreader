package feedreader.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * Utilities for managing users.
 * @author jared.pearson
 */
public class UserUtils {
	private DbUtils dbUtils;
	
	public UserUtils(@Nonnull DbUtils dbUtils) {
		Preconditions.checkArgument(dbUtils != null, "dbUtils should not be null");
		this.dbUtils = dbUtils;
	}
	
	/**
	 * Creates a new user with the specified email.
	 * @return the ID of the new user
	 */
	public int createUser(String email) throws SQLException {
		final Connection cnn = this.dbUtils.getConnection();
		try {
			return this.createUser(cnn, email);
		} finally {
			cnn.close();
		}
	}
	
	/**
	 * Creates a new user with the specified email.
	 * @return the ID of the new user
	 */
	private int createUser(Connection cnn, String email) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be empty");
		Preconditions.checkArgument(email != null && !email.isEmpty(), "email should not be empty");
		PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.Users (email) values (?) returning id");
		try {
			stmt.setString(1, email);
			
			boolean hasResult = stmt.execute();
			if(hasResult) {
				ResultSet rst = stmt.getResultSet();
				try {
					if(rst.next()) {
						return rst.getInt("id");
					} else {
						throw new IllegalStateException("Error while inserting user");
					}
				} finally {
					rst.close();
				}
			} else {
				throw new IllegalStateException("Error while inserting user");
			}
		} finally {
			stmt.close();
		}
	}
	
}
