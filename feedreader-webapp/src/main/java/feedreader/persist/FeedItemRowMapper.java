package feedreader.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.persist.RowMapper;

import feedreader.FeedItem;

public class FeedItemRowMapper implements RowMapper<FeedItem> {
	private String prefix;
	
	public FeedItemRowMapper() {
		this("feedItem_");
	}
	
	public FeedItemRowMapper(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public FeedItem mapRow(ResultSet rst) throws SQLException {
		FeedItem feedItem = new FeedItem();
		feedItem.setId(rst.getInt(prefix + "id"));
		feedItem.setTitle(rst.getString(prefix + "title"));
		feedItem.setDescription(rst.getString(prefix + "description"));
		feedItem.setLink(rst.getString(prefix + "link"));
		feedItem.setCreated(rst.getDate(prefix + "created"));
		feedItem.setPubDate(rst.getDate(prefix + "pubDate"));
		feedItem.setGuid(rst.getString(prefix + "guid"));
		feedItem.setFeedId(rst.getInt(prefix + "feedId"));
		
		return feedItem;
	}
}