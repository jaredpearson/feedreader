package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

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
				
				+ "f.createdBy feed_createdBy_id, "
				+ "u.email feed_createdBy_email "
				
				+ "from Feeds f "
				+ "inner join Users u on f.createdBy = u.id ";
		
		ROW_MAPPER = new FeedRowMapper(new UserRowMapper("feed_createdBy_"));
	}
	
	@Override
	public Object get(EntityManager.QueryContext queryContext, Object id) throws SQLException {
		Feed feed = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT
						+ "where f.id = ? "
						+ "limit 1");
				stmt.setInt(1, (Integer)id);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					if(rst.next()) {
						feed = ROW_MAPPER.mapRow(rst);
						
						//load the related feed itemsR
						List<FeedItem> feedItems = queryContext.getEntityManager().executeNamedQuery(FeedItem.class, "getFeedItemsForFeed", feed.getId());
						feed.setItems(feedItems);
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

	@Override
	public void persist(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		Feed feed = (Feed)entity;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into Feeds (url, lastUpdated, title, createdBy) values (?, ?, ?, ?) returning id, created");
				stmt.setString(1, feed.getUrl());
				stmt.setDate(2, toSqlDate(feed.getLastUpdated()));
				stmt.setString(3, feed.getTitle());
				
				if(feed.getCreatedBy() == null || feed.getCreatedBy().getId() == null) {
					stmt.setNull(4, Types.INTEGER);
				} else {
					stmt.setInt(4, feed.getCreatedBy().getId());
				}
				
				if(stmt.execute()) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						if(rst.next()) {
							feed.setId(rst.getInt(1));
							feed.setCreated(rst.getDate(2));
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
	public List<Object> executeNamedQuery(EntityManager.QueryContext queryContext, String query, Object... parameters)
			throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	private java.sql.Date toSqlDate(java.util.Date value) {
		if(value == null) {
			return null;
		}
		return new java.sql.Date(value.getTime());
	}
}
