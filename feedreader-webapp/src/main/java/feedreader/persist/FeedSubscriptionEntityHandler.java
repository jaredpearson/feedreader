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
				stmt = cnn.prepareStatement("select id, subscriber, feedId, created from feedreader.FeedSubscriptions fs "
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
	
	/**
	 * Inserts a new subscription for the given user (subscriber ID) to the given feed ID.
	 * @return the ID of the new subscription
	 */
	public int insert(Connection cnn, int subscriberId, int feedId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.FeedSubscriptions (subscriber, feedId) values (?, ?) returning id");
		try {
			stmt.setInt(1, subscriberId);
			stmt.setInt(2, feedId);
			
			if(stmt.execute()) {
				final ResultSet rst = stmt.getResultSet();
				try {
					if(rst.next()) {
						return rst.getInt(1);
					} else {
						throw new IllegalStateException("Unable to insert FeedSubscription");
					}
					
				} finally {
					DbUtils.close(rst);
				}
			} else {
				throw new IllegalStateException("Unable to insert FeedSubscription");
			}
			
		} finally {
			DbUtils.close(stmt);
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
