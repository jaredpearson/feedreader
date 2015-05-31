package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import common.persist.DbUtils;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;

/**
 * Persistence handler for the {@link FeedRequest} entity.
 * @author jared.pearson
 */
@Singleton
public class FeedRequestEntityHandler {
	private static final Logger logger = Logger.getLogger(FeedRequestEntityHandler.class.getName());

	/**
	 * Attempts to find the feed request with the specified ID value. If not found, then a null reference is returned.
	 * @return the feed request with the ID or null if not found
	 */
	public @Nullable FeedRequest findFeedRequestById(@Nonnull Connection cnn, int feedRequestId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");

		final PreparedStatement stmt = cnn.prepareStatement("select id, url, feedId, status, created, createdBy from feedreader.FeedRequests where id = ? limit 1");
		try {
			stmt.setInt(1, feedRequestId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if(rst.next()) {
					return mapRow(rst);
				} else { 
					return null;
				}
			} finally {
				DbUtils.close(rst);
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Updates the status of the request corresponding to the request ID.
	 * @return true when the update was successful
	 */
	public boolean updateRequestStatus(Connection cnn, int requestId, FeedRequestStatus status) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		Preconditions.checkArgument(status != null, "status should not be null");
		
		final PreparedStatement stmt = cnn.prepareStatement("update feedreader.FeedRequests set status = ? where id = ?");
		try {
			stmt.setString(1, status.getDbValue());
			stmt.setInt(2, requestId);
			
			return stmt.executeUpdate() > 0;
		} finally {
			DbUtils.close(stmt);
		}
	}

	/**
	 * Updates the feed ID and status of the request corresponding to the request ID.
	 * @return true when the update was successful
	 */
	public boolean updateRequestFeedAndStatus(Connection cnn, int requestId, int feedId, FeedRequestStatus status) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");
		Preconditions.checkArgument(status != null, "status should not be null");
		
		final PreparedStatement stmt = cnn.prepareStatement("update feedreader.FeedRequests set feedId = ?, status = ? where id = ?");
		try {
			stmt.setInt(1, feedId);
			stmt.setString(2, status.getDbValue());
			stmt.setInt(3, requestId);
			
			return stmt.executeUpdate() > 0;
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Inserts a new feed request with the given URL and createdBy user. The default status is NOT_STARTED.
	 * @return the ID of the new request
	 */
	public int insert(Connection cnn, String url, int createdByUserId) throws SQLException {
		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.FeedRequests (url, createdBy, status) values (?, ?, ?) returning id");
		try {
			stmt.setString(1, url);
			stmt.setInt(2, createdByUserId);
			stmt.setString(3, FeedRequestStatus.NOT_STARTED.getDbValue());
			
			if (stmt.execute()) {
				final ResultSet rst = stmt.getResultSet();
				try {
					if (rst.next()) {
						return rst.getInt("id");
					} else {
						throw new IllegalStateException("Unable to insert FeedRequest");
					}
				} finally {
					DbUtils.close(rst);
				}
			} else {
				throw new IllegalStateException("Unable to insert FeedRequest");
			}
		} finally {
			DbUtils.close(stmt);
		}
	}

	/**
	 * Gets the page of FeedRequest instances created by the given user ID.
	 * @param cnn the current connection 
	 * @param userId the ID of the user to retrieve the feed requests for
	 * @param pageIndex the zero-based index of the page to retrieve
	 * @param pageSize the size of the page to retrieve
	 * @return the feed request instances created by the user
	 */
	public @Nonnull List<FeedRequest> getFeedRequestsForUser(@Nonnull Connection cnn, int userId, int pageIndex, int pageSize) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");

		final PreparedStatement stmt = cnn.prepareStatement("select id, url, feedId, status, created, createdBy from feedreader.FeedRequests where createdBy = ? order by created desc limit " + pageSize + " offset " + (pageIndex * pageSize));
		try {
			stmt.setInt(1, userId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				final List<FeedRequest> feedRequests = Lists.newArrayListWithCapacity(pageSize);
				while (rst.next()) {
					feedRequests.add(mapRow(rst));
				}
				return feedRequests;
			} finally {
				DbUtils.close(rst);
			}
		} finally {
			DbUtils.close(stmt);
		}
	}
	
	/**
	 * Gets the total number of requests created by the given user ID.
	 * @param cnn the current connection 
	 * @param userId the ID of the user to retrieve the feed requests for
	 * @return the total number of requests
	 */
	public int getTotalNumberOfFeedRequestsForUser(Connection cnn, int userId) throws SQLException {
		Preconditions.checkArgument(cnn != null, "cnn should not be null");

		final PreparedStatement stmt = cnn.prepareStatement("select count(*) from feedreader.FeedRequests where createdBy = ?");
		try {
			stmt.setInt(1, userId);
			
			final ResultSet rst = stmt.executeQuery();
			try {
				if (rst.next()) {
					return rst.getInt(1);
				} else {
					logger.warning("Counting the FeedRequests was not executed correctly");
					return 0;
				}
			} finally {
				DbUtils.close(rst);
			}
		} finally {
			DbUtils.close(stmt);
		}
	}

	private FeedRequest mapRow(final ResultSet rst) throws SQLException {
		final FeedRequest feedRequest = new FeedRequest();
		feedRequest.setId(rst.getInt(1));
		feedRequest.setUrl(rst.getString("url"));
		feedRequest.setStatus(FeedRequestStatus.fromDbValue(rst.getString("status")));
		feedRequest.setCreated(rst.getTimestamp("created"));
		feedRequest.setCreatedById(rst.getInt("createdBy"));
		
		//get the referenced feed, which is optional
		int feedId = rst.getInt("feedId");
		if(!rst.wasNull()) {
			feedRequest.setFeedId(feedId); 
		}
		return feedRequest;
	}

}
