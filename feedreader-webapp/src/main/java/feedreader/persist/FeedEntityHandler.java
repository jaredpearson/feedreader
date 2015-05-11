package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import common.persist.DbUtils;
import common.persist.EntityManager;
import common.persist.RowMapper;
import common.persist.EntityManager.EntityHandler;
import feedreader.Feed;
import feedreader.FeedItem;

public class FeedEntityHandler implements EntityHandler {
	private static final String SELECT_SQL_FRAGMENT;
	private static final RowMapper<Feed> ROW_MAPPER;
	
	static {
		SELECT_SQL_FRAGMENT = "select "
				+ "f.id feed_id, "
				+ "f.url feed_url, "
				+ "f.lastUpdated feed_lastUpdated, "
				+ "f.title feed_title, "
				+ "f.created feed_created, "
				
				+ "f.createdBy feed_createdBy "
				
				+ "from feedreader.Feeds f "
				+ "inner join feedreader.Users u on f.createdBy = u.id ";
		
		ROW_MAPPER = new FeedRowMapper();
	}
	
	private final FeedItemEntityHandler feedItemEntityHandler;
	
	public FeedEntityHandler(FeedItemEntityHandler feedItemEntityHandler) {
		this.feedItemEntityHandler = feedItemEntityHandler;
	}
	
	public @Nullable Feed findFeedAndFeedItemsByFeedId(final Connection cnn, final int feedId) throws SQLException {
		Feed feed = null;
		
		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where f.id = ? limit 1");
		try {
			stmt.setInt(1, feedId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if(rst.next()) {
					feed = ROW_MAPPER.mapRow(rst);
					
					//load the related feed itemsR
					List<FeedItem> feedItems = feedItemEntityHandler.getFeedItemsForFeed(cnn, feed.getId());
					feed.setItems(feedItems);
				}
				
			} finally {
				DbUtils.close(rst);
			}
			
		} finally {
			DbUtils.close(stmt);
		}
		return feed;
	}
	
	@Override
	public Object get(EntityManager.QueryContext queryContext, Object id) throws SQLException {
		final Connection cnn = queryContext.getConnection();
		try {
			return findFeedAndFeedItemsByFeedId(cnn, (Integer) id);
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}

	@Override
	public List<?> executeNamedQuery(EntityManager.QueryContext queryContext, String query, Object... parameters)
			throws SQLException {
		
		if("findFeedByUrl".equalsIgnoreCase(query)) {
			Feed feed = findFeedByUrl(queryContext, (String)parameters[0]);
			if(feed == null) {
				return Collections.emptyList();
			} else {
				List<Feed> feeds = new ArrayList<Feed>();
				feeds.add(feed);
				return feeds;
			}
		} else {
			throw new UnsupportedOperationException("Unsupported query specified: " + query);
		}
		
	}
	
	/**
	 * Inserts a feed into the database.
	 * @return the ID of the new feed
	 */
	public int insert(Connection cnn, String url, java.util.Date lastUpdated, String title, int createdByUserId) throws SQLException {
		PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.Feeds (url, lastUpdated, title, createdBy) values (?, ?, ?, ?) returning id");
		try {
			stmt.setString(1, url);
			stmt.setDate(2, toSqlDate(lastUpdated));
			stmt.setString(3, title);
			stmt.setInt(4, createdByUserId);
			
			if(stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					if(rst.next()) {
						return rst.getInt("id");
					} else {
						throw new IllegalStateException("Unable to insert Feed");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			} else {
				throw new IllegalStateException("Unable to insert Feed");
			}
			
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Attempts to find the Feed with the specified URL.
	 */
	private Feed findFeedByUrl(EntityManager.QueryContext queryContext, String url) throws SQLException {
		Feed feed = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT
						+ "where lower(f.url) = lower(?) "
						+ "limit 1");
				stmt.setString(1, url);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					while(rst.next()) {
						feed = ROW_MAPPER.mapRow(rst);
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
		
		return feed;
	}
	
	private java.sql.Date toSqlDate(java.util.Date value) {
		if(value == null) {
			return null;
		}
		return new java.sql.Date(value.getTime());
	}
}
