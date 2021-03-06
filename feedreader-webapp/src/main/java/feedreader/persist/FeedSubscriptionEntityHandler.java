package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Singleton;

import common.persist.DbUtils;
import feedreader.FeedSubscription;

/**
 * Entity handler for persisting instances of {@link FeedSubscription}
 * @author jared.pearson
 */
@Singleton
public class FeedSubscriptionEntityHandler {
	
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
	
}
