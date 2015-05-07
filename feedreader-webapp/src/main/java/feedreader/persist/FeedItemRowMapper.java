package feedreader.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.persist.RowMapper;

import feedreader.Feed;
import feedreader.FeedItem;

public class FeedItemRowMapper implements RowMapper<FeedItem> {
	private String prefix;
	private FeedRowMapper feedRowMapper;
	
	public FeedItemRowMapper(FeedRowMapper feedRowMapper) {
		this("feedItem_", feedRowMapper);
	}
	
	public FeedItemRowMapper(String prefix, FeedRowMapper feedRowMapper) {
		this.prefix = prefix;
		this.feedRowMapper = feedRowMapper;
	}
	
	public FeedItem mapRow(ResultSet rst) throws SQLException {
		FeedItem feedItem = new FeedItem();
		feedItem.setId(rst.getInt(prefix + "id"));
		feedItem.setTitle(rst.getString(prefix + "title"));
		feedItem.setDescription(rst.getString(prefix + "description"));
		feedItem.setLink(rst.getString(prefix + "link"));
		feedItem.setCreated(rst.getDate(prefix + "created"));
		feedItem.setPubDate(rst.getDate(prefix + "pubDate"));
		feedItem.setGuid(rst.getString(prefix + "guid"));
		
		//get the feed
		Feed feed = feedRowMapper.mapRow(rst);
		feedItem.setFeed(feed);
		
		return feedItem;
	}
}