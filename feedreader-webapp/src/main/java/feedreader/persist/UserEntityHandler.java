package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import common.persist.DbUtils;
import common.persist.EntityManager;
import feedreader.User;

public class UserEntityHandler implements EntityManager.EntityHandler {
	
	@Override
	public void persist(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		User user = (User) entity;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into feedreader.Users (email) values (?) returning id");
				stmt.setString(1, user.getEmail());
				
				boolean hasResult = stmt.execute();
				if(hasResult) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						if(rst.next()) {
							user.setId(rst.getInt(1));
						}
					} finally {
						DbUtils.close(rst);
					}
				}
			} finally {
				DbUtils.close(stmt);
			}
			
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}
	
	@Override
	public Object get(EntityManager.QueryContext queryContext, Object id) throws SQLException {
		final Connection cnn = queryContext.getConnection();
		try {
			return loadUserById(cnn, (Integer)id);
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}
	
	@Override
	public List<Object> executeNamedQuery(EntityManager.QueryContext queryContext, String query, Object... parameters) throws SQLException {
		if("getUserByEmail".equals(query)) {
			return asList(getUserByEmail(queryContext, (String)parameters[0]));
		}
		
		throw new IllegalArgumentException("Unknown query specified: " + query);
	}
	
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
	
	private User getUserByEmail(EntityManager.QueryContext queryContext, String email) throws SQLException {
		User user = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("select id, email from feedreader.Users where email = ? limit 1");
				stmt.setString(1, email);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					
					if(rst.next()) {
						user = createUser(rst);
					}
					
				} finally {
					DbUtils.close(rst);
				}
			} finally {
				DbUtils.close(stmt);
			}
			
		} finally {
			queryContext.releaseConnection(cnn);
		}
		
		return user;
	}
	
	private User createUser(ResultSet rst) throws SQLException {
		User user = new User();
		user.setId(rst.getInt("id"));
		user.setEmail(rst.getString("email"));
		return user;
	}
	
	private List<Object> asList(User user) {
		if(user == null) {
			return Collections.emptyList();
		}
		ArrayList<Object> users = new ArrayList<Object>(1);
		users.add(user);
		return users;
	}
}
