package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import common.persist.DbUtils;
import common.persist.EntityManager;
import common.persist.RowMapper;
import common.persist.EntityManager.EntityHandler;
import common.persist.EntityManager.QueryContext;
import feedreader.FeedItem;

public class FeedItemEntityHandler implements EntityHandler {
	private static final String SELECT_SQL_FRAGMENT;
	private static final RowMapper<FeedItem> ROW_MAPPER;
	
	static {
		SELECT_SQL_FRAGMENT = "select "
			+ "i.id feedItem_id, "
			+ "i.title feedItem_title, "
			+ "i.description feedItem_description, "
			+ "i.link feedItem_link, "
			+ "i.pubDate feedItem_pubDate, "
			+ "i.guid feedItem_guid, "
			+ "i.created feedItem_created, "
			
			//feed
			+ "if.id feed_id, "
			+ "if.url feed_url, "
			+ "if.lastUpdated feed_lastUpdated, "
			+ "if.title feed_title, "
			+ "if.created feed_created, "
			
			//feed.createdBy
			+ "ifu.id feed_createdBy_id, "
			+ "ifu.email feed_createdBy_email "
			
			+ "from feedreader.FeedItems i "
			+ "inner join feedreader.Feeds if on i.feedId = if.id "
			+ "inner join feedreader.Users ifu on if.createdBy = ifu.id ";
		ROW_MAPPER = new FeedItemRowMapper(new FeedRowMapper("feed_", new UserRowMapper("feed_createdBy_")));
	}

	@Override
	public void persist(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		FeedItem feedItem = (FeedItem)entity;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into feedreader.FeedItems (feedId, title, description, link, pubDate, guid) values (?, ?, ?, ?, ?, ?) returning id, created");
				stmt.setInt(1, feedItem.getFeed().getId());
				stmt.setString(2, feedItem.getTitle());
				stmt.setString(3, feedItem.getDescription());
				stmt.setString(4, feedItem.getLink());
				DbUtils.setDate(stmt, 5, feedItem.getPubDate());
				stmt.setString(6, feedItem.getGuid());
				
				if(stmt.execute()) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						if(rst.next()) {
							feedItem.setId(rst.getInt(1));
							feedItem.setCreated(rst.getDate(2));
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
		FeedItem feedItem = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			ArrayList<Integer> feedItemIds = new ArrayList<Integer>(1);
			feedItemIds.add((Integer)id);
			
			List<FeedItem> feedItems = getFeedItems(cnn, feedItemIds);
			if(!feedItems.isEmpty()) {
				feedItem = feedItems.get(0);
			}
		} finally {
			DbUtils.close(cnn);
		}
		return feedItem;
	}

	@Override
	public List<?> executeNamedQuery(QueryContext queryContext, String query, Object... parameters)
			throws SQLException {
		if(query.equalsIgnoreCase("getFeedItemsForFeed")) {
			return getFeedItemsForFeed(queryContext, (Integer)parameters[0]);
		} else if(query.equalsIgnoreCase("getFeedItemsForStream")) {
			return getFeedItemsForStream(queryContext, (Integer)parameters[0], (Integer)parameters[1], (Integer)parameters[2]);
		} else {
			throw new UnsupportedOperationException("No method registered with name: " + query);
		}
	}
	
	/**
	 * Inserts a new feed item into the database
	 * @return the ID of the new feed item.
	 */
	public int insert(Connection cnn, int feedId, String title, String description, String link, Date pubDate, String guid) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.FeedItems (feedId, title, description, link, pubDate, guid) values (?, ?, ?, ?, ?, ?) returning id");
		try {
			stmt.setInt(1, feedId);
			stmt.setString(2, title);
			stmt.setString(3, description);
			stmt.setString(4, link);
			DbUtils.setDate(stmt, 5, pubDate);
			stmt.setString(6, guid);
			
			if (stmt.execute()) {
				ResultSet rst = null;
				try {
					rst = stmt.getResultSet();
					if (rst.next()) {
						return rst.getInt("id");
					} else {
						throw new IllegalStateException("Unable to create FeedItem");
					}
				} finally {
					DbUtils.close(rst);
				}
			} else {
				throw new IllegalStateException("Unable to create FeedItem");
			}
			
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Gets all of the feed items for the specified feed
	 */
	public List<FeedItem> getFeedItemsForFeed(QueryContext queryContext, int feedId) throws SQLException {
		List<FeedItem> feedItems = new ArrayList<FeedItem>();
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where if.id = ? ");
				stmt.setInt(1, feedId);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					while(rst.next()) {
						feedItems.add(ROW_MAPPER.mapRow(rst));
					}
					
				} finally {
					DbUtils.close(rst);
				}
				
			} finally {
				DbUtils.close(stmt);
			}
			
		} finally {
			DbUtils.close(cnn);
		}
		return feedItems;
	}

	/**
	 * Gets all of the feed items the user is subscribed to.
	 */
	public List<FeedItem> getFeedItemsForStream(QueryContext queryContext, int userId, int size, int offset) throws SQLException {
		List<FeedItem> feedItems = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			//get all of the feed item ids
			final List<Integer> feedItemIds = getSubscribedFeedItemIds(cnn, userId, size, offset);
			
			//get all of the feed items that match
			feedItems = getFeedItems(cnn, feedItemIds);
			
		} finally {
			DbUtils.close(cnn);
		}
		return feedItems;
	}
	
	/**
	 * Gets the feed item IDs for all of the subscriptions for the given user
	 */
	private List<Integer> getSubscribedFeedItemIds(Connection cnn, int userId, int size, int offset) throws SQLException {
		final ArrayList<Integer> feedItemIds = new ArrayList<Integer>(size);
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement("select fi.id feedItemId "
					+ "from feedreader.FeedSubscriptions fs inner join feedreader.FeedItems fi on fs.feedId = fi.feedId "
					+ "where fs.subscriber = ? "
					+ "order by fi.created desc limit " + size + " offset " + offset);
			stmt.setInt(1, userId);
			
			ResultSet rst = null;
			try {
				rst = stmt.executeQuery();
				while(rst.next()) {
					feedItemIds.add(rst.getInt("feedItemId"));
				}
			} finally {
				DbUtils.close(rst);
			}
		} finally {
			DbUtils.close(stmt);
		}
		return feedItemIds;
	}
	
	/**
	 * Gets all of the specified feed items corresponding to the specified IDs
	 */
	private List<FeedItem> getFeedItems(final Connection cnn, final List<Integer> feedItemIds) throws SQLException {
		final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>(feedItemIds.size());
		PreparedStatement stmt = null;
		try {
			stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT
					+ "where i.id = ? "
					+ "limit 1");
			
			//TODO: create a procedure to do all of the looping in Postgresql instead of doing in JDBC
			for(Integer feedItemId : feedItemIds) {
				stmt.setInt(1, feedItemId);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					
					//limit 1 clause used in query so there is no need to check anything after the first
					if(rst.next()) { 
						FeedItem feedItem = ROW_MAPPER.mapRow(rst);
						feedItems.add(feedItem);
					}
					
				} finally {
					DbUtils.close(rst);
				}
			}
			
		} finally {
			DbUtils.close(stmt);
		}
		return feedItems;
	}
}
