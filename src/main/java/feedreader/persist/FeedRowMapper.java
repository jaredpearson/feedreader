package feedreader.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.persist.RowMapper;
import feedreader.Feed;
import feedreader.User;

/**
 * Maps a row in a {@link ResultSet} to a {@link Feed}
 * @author jared.pearson
 */
public class FeedRowMapper implements RowMapper<Feed> {
	private String prefix;
	private UserRowMapper userRowMapper;
	
	public FeedRowMapper(UserRowMapper userRowMapper) {
		this("feed_", userRowMapper);
	}
	
	public FeedRowMapper(String prefix, UserRowMapper userRowMapper) {
		this.prefix = prefix;
		this.userRowMapper = userRowMapper;
	}
	
	@Override
	public Feed mapRow(ResultSet rst) throws SQLException {
		Feed feed = new Feed();
		feed.setId(rst.getInt(prefix + "id"));
		feed.setUrl(rst.getString(prefix + "url"));
		feed.setLastUpdated(rst.getDate(prefix + "lastUpdated"));
		feed.setTitle(rst.getString(prefix + "title"));
		feed.setCreated(rst.getDate(prefix + "created"));
		
		//FIXME: we could potential load the user's information more than once
		User createdByUser = userRowMapper.mapRow(rst);
		feed.setCreatedBy(createdByUser);
		
		return feed;
	}
	
}