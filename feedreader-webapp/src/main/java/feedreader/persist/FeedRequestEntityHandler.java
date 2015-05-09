package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import common.persist.DbUtils;
import common.persist.EntityManager.EntityHandler;
import common.persist.EntityManager.QueryContext;
import common.persist.RowMapper;
import feedreader.Feed;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;
import feedreader.User;

/**
 * Persistence handler for the {@link FeedRequest} entity.
 * @author jared.pearson
 */
public class FeedRequestEntityHandler implements EntityHandler {
	private static final RowMapper<FeedRequestData> ROW_MAPPER;
	
	static {
		ROW_MAPPER = new RowMapper<FeedRequestData>() {
			@Override
			public FeedRequestData mapRow(ResultSet rst) throws SQLException {
				FeedRequestData feedRequest = new FeedRequestData();
				feedRequest.id = rst.getInt(1);
				feedRequest.url = rst.getString("url");
				feedRequest.statusValue = rst.getString("status");
				
				//get the referenced feed, which is optional
				int feedId = rst.getInt("feedId");
				if(!rst.wasNull()) {
					feedRequest.feedId = feedId; 
				}
				
				feedRequest.created = rst.getDate(5);
				feedRequest.createdById = rst.getInt(6);
				return feedRequest;
			}
		};
	}
	
	@Override
	public void persist(QueryContext context, Object entity) throws SQLException {
		FeedRequest feedRequest = (FeedRequest) entity;
		if(feedRequest.getId() == null) {
			insert(context, feedRequest);
		} else {
			update(context, feedRequest);
		}
	}
	
	@Override
	public Object get(QueryContext queryContext, Object id) throws SQLException {
		FeedRequest feedRequest = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			// get the feed request data
			FeedRequestData feedRequestData = null;
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement(
						"select id, url, feedId, status, created, createdBy "
						+ "from feedreader.FeedRequests "
						+ "where id = ? "
						+ "limit 1");
				stmt.setInt(1, (Integer)id);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					if(rst.next()) {
						feedRequestData = ROW_MAPPER.mapRow(rst);
					}
				} finally {
					DbUtils.close(rst);
				}
				
			} finally {
				DbUtils.close(stmt);
			}

			
			// retrieve the referenced objects
			if(feedRequestData != null) {
				feedRequest = new FeedRequest();
				feedRequest.setId(feedRequestData.id);
				feedRequest.setStatus(FeedRequestStatus.fromDbValue(feedRequestData.statusValue));
				feedRequest.setCreated(feedRequestData.created);
				feedRequest.setUrl(feedRequestData.url);
				
				User createdBy = queryContext.getEntityManager().get(User.class, feedRequestData.createdById);
				feedRequest.setCreatedBy(createdBy);
				
				if(feedRequestData.feedId != null) {
					Feed feed = queryContext.getEntityManager().get(Feed.class, feedRequestData.feedId);
					feedRequest.setFeed(feed);
				}
			}
		} finally {
			queryContext.releaseConnection(cnn);
		}
		return feedRequest;
	}
	
	@Override
	public List<?> executeNamedQuery(QueryContext context, String query, Object... parameters) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void insert(QueryContext queryContext, FeedRequest feedRequest) throws SQLException {
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into feedreader.FeedRequests (url, feedId, status, createdBy) values (?, ?, ?, ?) returning id, created");
				stmt.setString(1, feedRequest.getUrl());
				DbUtils.setInt(stmt, 2, (feedRequest.getFeed() == null) ? null : feedRequest.getFeed().getId());
				stmt.setString(3, feedRequest.getStatus().getDbValue());
				DbUtils.setInt(stmt, 4, (feedRequest.getCreatedBy() == null) ? null : feedRequest.getCreatedBy().getId());
				
				if(stmt.execute()) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						if(rst.next()) {
							feedRequest.setId(rst.getInt(1));
							feedRequest.setCreated(rst.getDate(2));
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
	
	private void update(QueryContext queryContext, FeedRequest feedRequest) throws SQLException {
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("update feedreader.FeedRequests set url = ?, feedId = ?, status = ?, createdBy = ? where id = ?");
				stmt.setString(1, feedRequest.getUrl());
				DbUtils.setInt(stmt, 2, (feedRequest.getFeed() == null) ? null : feedRequest.getFeed().getId());
				stmt.setString(3, feedRequest.getStatus().getDbValue());
				DbUtils.setInt(stmt, 4, (feedRequest.getCreatedBy() == null) ? null : feedRequest.getCreatedBy().getId());
				stmt.setInt(5, feedRequest.getId());
				
				stmt.execute();
			} finally {
				DbUtils.close(stmt);
			}
			
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}
	
	private static class FeedRequestData {
		public int id;
		public String url;
		public Integer feedId;
		public String statusValue;
		public Date created;
		public int createdById;
	}
}
