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
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import common.persist.DbUtils;
import common.persist.EntityManager;
import common.persist.RowMapper;
import common.persist.EntityManager.EntityHandler;
import feedreader.Feed;
import feedreader.FeedItem;

@Singleton
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
	
	@Inject
	public FeedEntityHandler(FeedItemEntityHandler feedItemEntityHandler) {
		this.feedItemEntityHandler = feedItemEntityHandler;
	}

	/**
	 * Finds the feed that corresponds to the specified ID. If no feed matches, then a null reference is returned.
	 */
	public @Nullable Feed findFeedAndFeedItemsByFeedId(final Connection cnn, final int feedId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		Feed feed = null;
		
		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where f.id = ? limit 1");
		try {
			stmt.setInt(1, feedId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if(rst.next()) {
					feed = ROW_MAPPER.mapRow(rst);
					
					//load the related feed items
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

	/**
	 * Finds the feed that corresponds to the specified URL. If no feed matches, then a null reference is returned.
	 */
	public @Nullable Feed findFeedAndFeedItemsByUrl(@Nonnull final Connection cnn, @Nonnull final String url) throws SQLException {
		return findFeedAndFeedItemsByUrl(cnn, url, null);
	}
	
	/**
	 * Finds the feed that corresponds to the specified URL. If no feed matches, then a null reference is returned.
	 * @param feedItemLimit the max number of feed items to retrieve. set to null to retrieve all of the feed items 
	 */
	public @Nullable Feed findFeedAndFeedItemsByUrl(@Nonnull final Connection cnn, @Nonnull final String url, @Nullable final Integer feedItemLimit) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		Preconditions.checkArgument(url != null && !url.isEmpty(), "url should not be null");
		Feed feed = null;
		
		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where lower(f.url) = lower(?) limit 1");
		try {
			stmt.setString(1, url);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				
				if (rst.next()) {
					feed = ROW_MAPPER.mapRow(rst);

					//load the related feed items
					List<FeedItem> feedItems = feedItemEntityHandler.getFeedItemsForFeed(cnn, feed.getId(), feedItemLimit);
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
		
		Connection cnn = queryContext.getConnection();
		try {
			
			return findFeedAndFeedItemsByUrl(cnn, url);
			
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}
	
	private java.sql.Date toSqlDate(java.util.Date value) {
		if(value == null) {
			return null;
		}
		return new java.sql.Date(value.getTime());
	}
}
