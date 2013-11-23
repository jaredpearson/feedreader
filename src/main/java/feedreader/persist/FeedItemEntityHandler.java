package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
			
			+ "from FeedItems i "
			+ "inner join Feeds if on i.feedId = if.id "
			+ "inner join Users ifu on if.createdBy = ifu.id ";
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
				stmt = cnn.prepareStatement("insert into FeedItems (feedId, title, description, link, pubDate, guid) values (?, ?, ?, ?, ?, ?) returning id, created");
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
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT
						+ "where i.id = ? "
						+ "limit 1");
				stmt.setInt(1, (Integer)id);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					if(rst.next()) {
						feedItem = ROW_MAPPER.mapRow(rst);
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
		return feedItem;
	}

	@Override
	public List<?> executeNamedQuery(QueryContext queryContext, String query, Object... parameters)
			throws SQLException {
		if(query.equalsIgnoreCase("getFeedItemsForFeed")) {
			return getFeedItemsForFeed(queryContext, (Integer)parameters[0]);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
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
}
