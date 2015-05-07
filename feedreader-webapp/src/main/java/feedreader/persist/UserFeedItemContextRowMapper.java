package feedreader.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.persist.RowMapper;
import feedreader.FeedItem;
import feedreader.UserFeedItemContext;

/**
 * Maps a row within a {@link ResultSet} to a {@link UserFeedItemContext}
 * @author jared.pearson
 */
public class UserFeedItemContextRowMapper implements RowMapper<UserFeedItemContext> {
	private String prefix;
	private FeedItemRowMapper feedItemRowMapper;
	
	public UserFeedItemContextRowMapper(String prefix, FeedItemRowMapper feedItemRowMapper) {
		this.prefix = prefix;
		this.feedItemRowMapper = feedItemRowMapper;
	}
	
	public UserFeedItemContextRowMapper(FeedItemRowMapper feedItemRowMapper) {
		this("context_", feedItemRowMapper);
	}
	
	@Override
	public UserFeedItemContext mapRow(ResultSet rst) throws SQLException {
		UserFeedItemContext feedItemContext = new UserFeedItemContext();
		feedItemContext.setId(rst.getInt(prefix + "id"));
		feedItemContext.setRead(rst.getBoolean(prefix + "read"));
		feedItemContext.setCreated(rst.getDate(prefix + "created"));
		
		//get the associated feed item
		FeedItem feedItem = feedItemRowMapper.mapRow(rst);
		feedItemContext.setFeedItem(feedItem);
		
		return feedItemContext;
	}
}