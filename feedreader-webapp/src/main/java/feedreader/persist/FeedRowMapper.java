package feedreader.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.persist.RowMapper;
import feedreader.Feed;

/**
 * Maps a row in a {@link ResultSet} to a {@link Feed}
 * @author jared.pearson
 */
public class FeedRowMapper implements RowMapper<Feed> {
	private String prefix;
	
	public FeedRowMapper() {
		this("feed_");
	}
	
	public FeedRowMapper(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public Feed mapRow(ResultSet rst) throws SQLException {
		Feed feed = new Feed();
		feed.setId(rst.getInt(prefix + "id"));
		feed.setUrl(rst.getString(prefix + "url"));
		feed.setLastUpdated(rst.getDate(prefix + "lastUpdated"));
		feed.setTitle(rst.getString(prefix + "title"));
		feed.setCreated(rst.getDate(prefix + "created"));
		feed.setCreatedById(rst.getInt(prefix + "createdBy"));
		return feed;
	}
	
}