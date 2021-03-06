package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import common.persist.DbUtils;
import feedreader.FeedItem;
import feedreader.UserFeedItemContext;

@Singleton
public class UserFeedItemContextEntityHandler {
	private static final String SELECT_SQL_FRAGMENT;
	private static final FeedItemRowMapper ROW_MAPPER_FEED_ITEM;
	
	static {
		SELECT_SQL_FRAGMENT = "select "
				+ "c.id context_id, "
				+ "c.read context_read, "
				+ "c.created context_created, "
				+ "c.feedItemId context_feedItemId, "
				+ "c.owner context_owner, "
				
				//feedItem
				+ "ci.id feedItem_id, "
				+ "ci.title feedItem_title, "
				+ "ci.description feedItem_description, "
				+ "ci.link feedItem_link, "
				+ "ci.pubDate feedItem_pubDate, "
				+ "ci.guid feedItem_guid, "
				+ "ci.created feedItem_created, "
				+ "ci.feedId feedItem_feedId "
				
				+ "from feedreader.UserFeedItemContexts c "
				+ "inner join feedreader.FeedItems ci on c.feedItemId = ci.id ";
		
		ROW_MAPPER_FEED_ITEM = new FeedItemRowMapper("feedItem_");
	}
	
	/**
	 * Gets the feed item for the given user ID and feed item ID. If no context has been established in the DB for the user and feed item, then a
	 * null reference is returned.
	 * @param cnn the current DB connection to use
	 * @param userId the ID of the user
	 * @param feedItemId the ID of the feed item
	 * @return the feed item for the given user ID and feed item ID or null if no context has been established yet.
	 */
	public @Nullable UserFeedItemContext getFeedItem(Connection cnn, int userId, int feedItemId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where c.owner = ? and c.feedItemId = ? limit 1");
		try {
			stmt.setInt(1, userId);
			stmt.setInt(2, feedItemId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if(rst.next()) {
					return mapRow(rst);
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
	
	public @Nonnull List<UserFeedItemContext> getFeedItemsForUserFeed(Connection cnn, int userId, int feedId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		final List<UserFeedItemContext> feedItemContexts = new ArrayList<UserFeedItemContext>();
		
		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where c.owner = ? and ci.feedId = ? ");
		try {
			stmt.setInt(1, userId);
			stmt.setInt(2, feedId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				while(rst.next()) {
					feedItemContexts.add(mapRow(rst));
				}
				
			} finally {
				DbUtils.close(rst);
			}
		} finally {
			DbUtils.close(stmt);
		}
		
		return feedItemContexts;
	}
	
	/**
	 * Gets all of the context items for the given feed item IDs. If there is no context for the given IDs, then it is not represented
	 * in the list.
	 */
	public @Nonnull List<UserFeedItemContext> getUserFeedItemsForFeedItems(Connection cnn, int userId, Set<Integer> feedItemIds) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		
		if (feedItemIds == null || feedItemIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		final List<UserFeedItemContext> feedItemContexts = Lists.newArrayListWithExpectedSize(feedItemIds.size());
		
		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where c.owner = ? and ci.id = ? limit 1");
		try {
			for(Integer feedItemId : feedItemIds) {
				stmt.clearWarnings();
				stmt.clearParameters();

				//set the parameters
				stmt.setInt(1, userId);
				stmt.setInt(2, feedItemId);
				
				//execute the query
				ResultSet rst = stmt.executeQuery();
				try {
					
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
		
		return feedItemContexts;
	}
	
	/**
	 * Loads the read status from the database for the feed item context with the given feed item ID and owner ID. If the record does not exist, a null
	 * value is returned otherwise the value of the field is returned.
	 * @return the value of the read status field or null if the read status field has not been set yet.
	 */
	public Boolean loadReadStatus(Connection cnn, int feedItemId, int ownerId) throws SQLException {
		final PreparedStatement selectStmt = cnn.prepareStatement("select read from feedreader.UserFeedItemContexts where owner = ? and feedItemId = ? limit 1");
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
		PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.UserFeedItemContexts (feedItemId, owner, read) values (?, ?, ?) returning id");
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
