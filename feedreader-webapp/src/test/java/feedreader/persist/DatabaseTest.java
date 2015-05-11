package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import common.persist.ConnectionHandler;
import common.persist.DbUtils;
import common.persist.EntityManager;

public class DatabaseTest {
	private DataSource dataSource;
	private Integer testUserId;
	private Integer testFeedId;
	private Integer testFeedItemId;
	private Integer testUserFeedItemContextId;
	
	protected Connection getConnection() throws SQLException {
		Connection cnn = getDataSource().getConnection();
		cnn.setAutoCommit(false);
		cnn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		return cnn;
	}
	
	protected Object withConnection(ConnectionHandler handler) throws SQLException {
		Connection cnn = null;
		try {
			cnn = getConnection();
			return handler.handle(cnn);
		} finally {
			DbUtils.close(cnn);
		}
	}
	
	protected DataSource getDataSource() {
		if(dataSource == null) {
			PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
			pgDataSource.setServerName("192.168.52.13");
			pgDataSource.setPortNumber(5432);
			pgDataSource.setDatabaseName("feedreader");
			pgDataSource.setUser("feedreader_app");
			pgDataSource.setPassword("zUSAC7HbtXcVMkk");
			this.dataSource = pgDataSource;
		}
		return dataSource;
	}
	
	protected EntityManager.QueryContext createQueryContext(final Connection cnn, final EntityManager entityManager) {
		return new EntityManager.QueryContext() {
			@Override
			public void releaseConnection(Connection cnn) throws SQLException {
				
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				return cnn;
			}
			
			@Override
			public EntityManager getEntityManager() {
				return entityManager;
			}
		};
	}
	
	protected EntityManager.QueryContext createQueryContext(final Connection cnn) {
		return createQueryContext(cnn, null);
	}
	
	protected int ensureTestUser(Connection cnn) throws SQLException {
		if(testUserId == null) {
			testUserId = insertTestUser(cnn);
		}
		return testUserId;
	}
	
	/**
	 * Inserts a test user (without using the persistence framework) and returns the 
	 * test user's ID
	 */
	protected int insertTestUser(Connection cnn) throws SQLException {
		int testUserId = -1;
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement("insert into feedreader.Users (email) values (?) returning id");
			stmt.setString(1, "test@test.com");
			
			if(stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					if(rst.next()) {
						testUserId = rst.getInt("id");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			}
		} finally {
			DbUtils.close(stmt);
		}
		
		return testUserId;
	}
	
	protected int ensureTestFeed(Connection cnn) throws SQLException {
		if(testFeedId == null) {
			testFeedId = insertTestFeed(cnn);
		}
		return testFeedId;
	}
	
	protected int insertTestFeed(Connection cnn) throws SQLException {
		int testUserId = ensureTestUser(cnn);
		
		int testFeedId = -1;
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement("insert into feedreader.Feeds (url, title, lastUpdated, createdBy) values (?, ?, ?, ?) returning id");
			stmt.setString(1, "http://test.com/test.xml");
			stmt.setString(2, "Test Feed");
			setDate(stmt, 3, 2013, 6, 10);
			
			stmt.setInt(4, testUserId);
			
			if(stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					if(rst.next()) {
						testFeedId = rst.getInt("id");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			}
		} finally {
			DbUtils.close(stmt);
		}
		return testFeedId;
	}
	
	protected int ensureTestFeedItem(Connection cnn) throws SQLException {
		if(testFeedItemId == null) {
			testFeedItemId = insertTestFeedItem(cnn);
		}
		return testFeedItemId;
 	}
	
	protected int insertTestFeedItem(Connection cnn) throws SQLException {
		int testFeedId = ensureTestFeed(cnn);
		int testFeedItemId = -1;
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement("insert into feedreader.FeedItems (feedId, title, description, link) values (?, ?, ?, ?) returning id");
			stmt.setInt(1, testFeedId);
			stmt.setString(2, "Article 1");
			stmt.setString(3, "Article 1 body");
			stmt.setString(4, "http://test.com/test.xml/1");
			
			if(stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					if(rst.next()) {
						testFeedItemId = rst.getInt("id");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			}
		} finally {
			DbUtils.close(stmt);
		}
		return testFeedItemId;
	}
	
	protected int ensureTestUserFeedItemContext(Connection cnn) throws SQLException {
		if(testUserFeedItemContextId == null) {
			testUserFeedItemContextId = insertTestUserFeedItemContext(cnn);
		}
		return testUserFeedItemContextId;
	}
	
	protected int insertTestUserFeedItemContext(Connection cnn) throws SQLException {
		int testFeedItemId = ensureTestFeedItem(cnn);
		int testUserId = ensureTestUser(cnn);
		int testContextId = -1;
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement("insert into feedreader.UserFeedItemContexts (feedItemId, owner, read) values (?, ?, ?) returning id");
			stmt.setInt(1, testFeedItemId);
			stmt.setInt(2, testUserId);
			stmt.setBoolean(3, false);
			
			if(stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					if(rst.next()) {
						testContextId = rst.getInt("id");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			}
		} finally {
			DbUtils.close(stmt);
		}
		return testContextId;
	}
	
	private void setDate(PreparedStatement stmt, int parameterIndex, int year, int month, int date) throws SQLException {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month, date);
		stmt.setDate(parameterIndex, new java.sql.Date(cal.getTimeInMillis()));
	}
}
