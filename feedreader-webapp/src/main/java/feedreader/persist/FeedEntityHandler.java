package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import common.persist.DbUtils;
import feedreader.Feed;
import feedreader.FeedItem;

@Singleton
public class FeedEntityHandler {
	private static final String SELECT_SQL_FRAGMENT;
	
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
	}
	
	private final FeedItemEntityHandler feedItemEntityHandler;
	
	@Inject
	public FeedEntityHandler(FeedItemEntityHandler feedItemEntityHandler) {
		this.feedItemEntityHandler = feedItemEntityHandler;
	}

	/**
	 * Finds all feeds corresponding from the given feed IDs.
	 * @param cnn the current connection
	 * @param feedIds the IDs of the all the feeds to load. passing null or empty set will yield an empty map
	 * @param feedItemLimit the number of feed items to load. passing null will load the default number of feed items
	 */
	public @Nonnull Map<Integer, Feed> findFeedsAndFeedItemsByFeedIds(final Connection cnn, final Set<Integer> feedIds, final Integer feedItemLimit) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		if (feedIds == null || feedIds.isEmpty()) {
			return Collections.emptyMap();
		}
		
		// build the SQL statement
		// TODO: instead of creating a dynamic SQL statement, build a stored procedure that can take in the IDs and do this within PostgreSQL 
		final StringBuilder sqlStringBuilder = new StringBuilder();
		sqlStringBuilder.append(SELECT_SQL_FRAGMENT);
		sqlStringBuilder.append(" where f.id in (");
		boolean first = true;
		for (Integer feedId : feedIds) {
			if (feedId != null) {
				if (!first) {
					sqlStringBuilder.append(", ");
				}
				sqlStringBuilder.append(feedId);
				first = false;
			}
		}
		sqlStringBuilder.append(")");
		
		final Map<Integer, Feed> feedsById = Maps.newHashMapWithExpectedSize(feedIds.size());
		final PreparedStatement stmt = cnn.prepareStatement(sqlStringBuilder.toString());
		try {
			final ResultSet rst = stmt.executeQuery();
			try {
				while (rst.next()) {
					int feedId = rst.getInt("feed_id");
					
					final Feed feed = mapRow(rst);
					
					//load the related feed items
					List<FeedItem> feedItems = feedItemEntityHandler.getFeedItemsForFeed(cnn, feedId, feedItemLimit);
					feed.setItems(feedItems);
					
					feedsById.put(feedId, feed);
				}
				
			} finally {
				DbUtils.close(rst);
			}
			
		} finally {
			DbUtils.close(stmt);
		}
		return Collections.unmodifiableMap(feedsById);
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
					feed = mapRow(rst);
					
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
					feed = mapRow(rst);

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
	
	/**
	 * Inserts a feed into the database.
	 * @return the ID of the new feed
	 */
	public int insert(@Nonnull Connection cnn, String url, java.util.Date lastUpdated, String title, int createdByUserId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.Feeds (url, lastUpdated, title, createdBy) values (?, ?, ?, ?) returning id");
		try {
			stmt.setString(1, url);
			DbUtils.setTimestamp(stmt, 2, lastUpdated);
			stmt.setString(3, title);
			stmt.setInt(4, createdByUserId);
			
			if(stmt.execute()) {
				final ResultSet rst = stmt.getResultSet();
				try { 
					
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
	
	private Feed mapRow(ResultSet rst) throws SQLException {
		Feed feed = new Feed();
		feed.setId(rst.getInt("feed_id"));
		feed.setUrl(rst.getString("feed_url"));
		feed.setLastUpdated(rst.getTimestamp("feed_lastUpdated"));
		feed.setTitle(rst.getString("feed_title"));
		feed.setCreated(rst.getTimestamp("feed_created"));
		feed.setCreatedById(rst.getInt("feed_createdBy"));
		return feed;
	}
}
