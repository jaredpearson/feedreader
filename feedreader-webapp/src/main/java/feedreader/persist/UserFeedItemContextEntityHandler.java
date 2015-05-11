package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import common.persist.DbUtils;
import common.persist.EntityManager;
import common.persist.EntityManager.EntityHandler;
import feedreader.FeedItem;
import feedreader.UserFeedItemContext;

public class UserFeedItemContextEntityHandler implements EntityHandler {
	private static final String SELECT_SQL_FRAGMENT;
	private static final FeedItemRowMapper ROW_MAPPER_FEED_ITEM;
	
	static {
		SELECT_SQL_FRAGMENT = "select "
				+ "c.id context_id, "
				+ "c.read context_read, "
				+ "c.created context_created, "
				+ "c.feedItemId context_feedItemId, "
				
				//owner
				+ "c.owner context_owner, "
				
				//feedItem
				+ "ci.id feedItem_id, "
				+ "ci.title feedItem_title, "
				+ "ci.description feedItem_description, "
				+ "ci.link feedItem_link, "
				+ "ci.pubDate feedItem_pubDate, "
				+ "ci.guid feedItem_guid, "
				+ "ci.created feedItem_created, "
				
				//feedItem.feed
				+ "cif.id feed_id, "
				+ "cif.url feed_url, "
				+ "cif.lastUpdated feed_lastUpdated, "
				+ "cif.title feed_title, "
				+ "cif.created feed_created, "
				
				//feedItem.feed.createdBy
				+ "cifu.id feed_createdBy_id, "
				+ "cifu.email feed_createdBy_email "
				
				+ "from feedreader.UserFeedItemContexts c "
				+ "inner join feedreader.Users cu on c.owner = cu.id "
				+ "inner join feedreader.FeedItems ci on c.feedItemId = ci.id "
				+ "inner join feedreader.Feeds cif on ci.feedId = cif.id "
				+ "inner join feedreader.Users cifu on cif.createdBy = cifu.id ";
		
		ROW_MAPPER_FEED_ITEM = new FeedItemRowMapper("feedItem_", new FeedRowMapper("feed_", new UserRowMapper("feed_createdBy_")));
	}
	
	@Override
	public void persist(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		UserFeedItemContext feedItemContext = (UserFeedItemContext)entity;
		if(feedItemContext.getId() == null) {
			insert(queryContext, feedItemContext);
		} else {
			update(queryContext, feedItemContext);
		}
	}

	@Override
	public Object get(EntityManager.QueryContext queryContext, Object id) throws SQLException {
		UserFeedItemContext feedItemContext = null;
		int contextId = (Integer)id;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT
						+ "where c.id = ? "
						+ "limit 1");
				stmt.setInt(1, contextId);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					if(rst.next()) {
						feedItemContext = mapRow(rst);
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
		
		return feedItemContext;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<?> executeNamedQuery(EntityManager.QueryContext queryContext, String query, Object... parameters)
			throws SQLException {
		if(query.equalsIgnoreCase("getFeedItemsForUserFeed")) {
			return getFeedItemsForUserFeed(queryContext, (Integer)parameters[0], (Integer)parameters[1]);
		} else if(query.equalsIgnoreCase("getUserFeedItemsForFeedItems")) {
			return getUserFeedItemsForFeedItems(queryContext, (Integer)parameters[0], (Set<Integer>)parameters[1]);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public List<UserFeedItemContext> getFeedItemsForUserFeed(EntityManager.QueryContext queryContext, int userId, int feedId) throws SQLException {
		List<UserFeedItemContext> feedItemContexts = new ArrayList<UserFeedItemContext>();
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where c.owner = ? and cif.id = ? ");
				stmt.setInt(1, userId);
				stmt.setInt(2, feedId);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					while(rst.next()) {
						feedItemContexts.add(mapRow(rst));
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
		
		return feedItemContexts;
	}
	
	public List<UserFeedItemContext> getUserFeedItemsForFeedItems(EntityManager.QueryContext queryContext, int userId, Set<Integer> feedItemIds) throws SQLException {
		List<UserFeedItemContext> feedItemContexts = new ArrayList<UserFeedItemContext>();
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where c.owner = ? and ci.id = ? ");
				for(Integer feedItemId : feedItemIds) {
					stmt.clearWarnings();
					stmt.clearParameters();

					//set the parameters
					stmt.setInt(1, userId);
					stmt.setInt(2, feedItemId);
					
					//execute the query
					ResultSet rst = null;
					try {
						rst = stmt.executeQuery();
						while(rst.next()) {
							feedItemContexts.add(mapRow(rst));
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
		
		return feedItemContexts;
	}
	
	private void insert(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		UserFeedItemContext feedItemContext = (UserFeedItemContext)entity;
		
		validate(feedItemContext, true);
		
		final Integer feedItemId = feedItemContext.getFeedItem().getId();
		final Integer ownerId = feedItemContext.getOwner().getId();
		final boolean readStatus = feedItemContext.isRead();
		
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into feedreader.UserFeedItemContexts (feedItemId, owner, read) values (?, ?, ?) returning id, read, created");
				stmt.setInt(1, feedItemId);
				stmt.setInt(2, ownerId);
				stmt.setBoolean(3, readStatus);
				
				if(stmt.execute()) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						if(rst.next()) {
							feedItemContext.setId(rst.getInt("id"));
							feedItemContext.setRead(rst.getBoolean("read"));
							feedItemContext.setCreated(rst.getDate("created"));
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
	
	/**
	 * Loads the read status from the database for the feed item context with the given feed item ID and owner ID. If the record does not exist, a null
	 * value is returned otherwise the value of the field is returned.
	 * @return the value of the read status field or null if the read status field has not been set yet.
	 */
	public Boolean loadReadStatus(Connection cnn, int feedItemId, int ownerId) throws SQLException {
		final PreparedStatement selectStmt = cnn.prepareStatement("select read from feedreader.UserFeedItemContexts where owner = ? and feedItemId = ?");
		try {
			selectStmt.setInt(1, ownerId);
			selectStmt.setInt(2, feedItemId);
			
			final ResultSet selectRst = selectStmt.executeQuery();
			try {
				if (selectRst.next()) {
					return selectRst.getBoolean("read");
				} else {
					return null;
				}
			} finally {
				selectRst.close();
			}
		} finally {
			selectStmt.close();
		}
	}
	
	public int insert(Connection cnn, int feedItemId, int ownerId, boolean readStatus) throws SQLException {
		PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.UserFeedItemContexts (feedItemId, owner, read) values (?, ?, ?) returning id, read, created");
		try {
			stmt.setInt(1, feedItemId);
			stmt.setInt(2, ownerId);
			stmt.setBoolean(3, readStatus);
			
			if(stmt.execute()) {
				final ResultSet rst = stmt.getResultSet();
				try {
					if(rst.next()) {
						return rst.getInt("id");
					} else {
						throw new IllegalStateException("Unable to insert UserFeedItemContext entity");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			} else {
				throw new IllegalStateException("Unable to insert UserFeedItemContext entity");
			}
			
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	public boolean updateReadStatus(Connection cnn, int feedItemId, int ownerId, boolean readStatus) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("update feedreader.UserFeedItemContexts set read = ? where owner = ? and feedItemId = ?");
		try {
			stmt.setBoolean(1, readStatus);
			stmt.setInt(2, ownerId);
			stmt.setInt(3, feedItemId);
			
			final int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		} finally {
			stmt.close();
		}
	}
	
	private void update(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		UserFeedItemContext feedItemContext = (UserFeedItemContext)entity;
		
		validate(feedItemContext, false);
		
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("update feedreader.UserFeedItemContexts set read = ? where id = ?");
				stmt.setBoolean(1, feedItemContext.isRead());
				stmt.setInt(2, feedItemContext.getId());
				
				if(stmt.executeUpdate() != 1) {
					//TODO: log a message that nothing was updated
				}
				
			} finally {
				DbUtils.close(stmt);
			}
			
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}
	
	/**
	 * FIXME: Validation should be able to throw more than 1 exception
	 */
	private void validate(UserFeedItemContext context, boolean insert) {
		if(context.getFeedItemId() == null) {
			//FIXME: throw a more specific exception during validation
			throw new IllegalStateException("FeedItem is required");
		}
		
		if(context.getOwner() == null) {
			//FIXME: throw a more specific exception during validation
			throw new IllegalStateException("Owner is required");
		}
	}
	
	private UserFeedItemContext mapRow(ResultSet rst) throws SQLException {
		final int feedItemContextId = rst.getInt("context_id");
		final int feedItemId = rst.getInt("context_feedItemId");
		
		final UserFeedItemContext feedItemContext = new UserFeedItemContext();
		feedItemContext.setId(feedItemContextId);
		feedItemContext.setRead(rst.getBoolean("context_read"));
		feedItemContext.setCreated(rst.getDate("context_created"));
		
		//get the associated feed item
		final FeedItem feedItem = ROW_MAPPER_FEED_ITEM.mapRow(rst);
		if (feedItem == null) {
			throw new IllegalStateException(String.format("Feed item context %d references an invalid feed item ID: %d", feedItemContextId, feedItemId));
		}
		feedItemContext.setFeedItem(feedItem);
		feedItemContext.setFeedItemId(feedItemId);
		
		return feedItemContext;
	}
}
