package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import common.persist.DbUtils;
import common.persist.EntityManager.EntityHandler;
import common.persist.EntityManager.QueryContext;
import feedreader.FeedRequest;
import feedreader.FeedRequestStatus;

/**
 * Persistence handler for the {@link FeedRequest} entity.
 * @author jared.pearson
 */
public class FeedRequestEntityHandler implements EntityHandler {

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
	
	@Override
	public List<?> executeNamedQuery(QueryContext context, String query, Object... parameters) throws SQLException {
		// TODO Auto-generated method stub
		return null;
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
}
