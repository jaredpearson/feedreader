package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import common.persist.DbUtils;
import common.persist.EntityManager.EntityHandler;
import common.persist.EntityManager.QueryContext;
import feedreader.Feed;
import feedreader.FeedSubscription;
import feedreader.User;

/**
 * Entity handler for persisting instances of {@link FeedSubscription}
 * @author jared.pearson
 */
public class FeedSubscriptionEntityHandler implements EntityHandler {
	
	@Override
	public Object get(QueryContext queryContext, Object id) throws SQLException {
		FeedSubscriptionData feedSubscriptionData = null;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("select id, subscriber, feedId, created from FeedSubscriptions fs "
						+ "where fs.id = ? "
						+ "limit 1");
				stmt.setInt(1, (Integer)id);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					if(rst.next()) {
						feedSubscriptionData = new FeedSubscriptionData();
						feedSubscriptionData.id = rst.getInt("id");
						feedSubscriptionData.subscriberId = rst.getInt("subscriber");
						feedSubscriptionData.feedId = rst.getInt("feedId");
						feedSubscriptionData.created = rst.getDate("created");
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
		
		FeedSubscription feedSubscription = null;
		
		//get the related objects
		if(feedSubscriptionData != null) {
			feedSubscription = new FeedSubscription();
			feedSubscription.setId(feedSubscriptionData.id);
			feedSubscription.setCreated(feedSubscriptionData.created);
			
			//get the subscriber
			User subscriber = queryContext.getEntityManager().get(User.class, feedSubscriptionData.subscriberId);
			feedSubscription.setSubscriber(subscriber);
			
			//get the feed
			Feed feed = queryContext.getEntityManager().get(Feed.class, feedSubscriptionData.feedId);
			feedSubscription.setFeed(feed);
		}
		
		return feedSubscription;
	}
	
	@Override
	public void persist(QueryContext queryContext, Object entity) throws SQLException {
		FeedSubscription feedSubscription = (FeedSubscription)entity;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into FeedSubscriptions (subscriber, feedId) values (?, ?) returning id, created");
				
				if(feedSubscription.getSubscriber() == null || feedSubscription.getSubscriber().getId() == null) {
					stmt.setNull(1, Types.INTEGER);
				} else {
					stmt.setInt(1, feedSubscription.getSubscriber().getId());
				}
				
				if(feedSubscription.getFeed() == null || feedSubscription.getFeed().getId() == null) {
					stmt.setNull(2, Types.INTEGER);
				} else {
					stmt.setInt(2, feedSubscription.getFeed().getId());
				}
				
				if(stmt.execute()) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						if(rst.next()) {
							feedSubscription.setId(rst.getInt(1));
							feedSubscription.setCreated(rst.getDate(2));
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
	public List<?> executeNamedQuery(QueryContext context, String query,
			Object... parameters) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	private static class FeedSubscriptionData {
		public int id;
		public int subscriberId;
		public int feedId;
		public Date created;
	}
}
