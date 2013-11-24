package feedreader.web.rest;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import common.json.JsonMapper;
import common.json.JsonWriter;
import feedreader.UserFeedItemContext;

class UserFeedItemContextJsonMapper implements JsonMapper<UserFeedItemContext> { 
	private static final UserFeedItemContextJsonMapper INSTANCE;
	private static final Set<String> ALL_FIELDS;
	private Set<String> fields;
	
	static {
		ALL_FIELDS = new HashSet<String>();
		ALL_FIELDS.add("read");
		ALL_FIELDS.add("title");
		ALL_FIELDS.add("pubDate");
		ALL_FIELDS.add("guid");
		ALL_FIELDS.add("feed");
		
		INSTANCE = new UserFeedItemContextJsonMapper(ALL_FIELDS);
	}
	
	private UserFeedItemContextJsonMapper(final Set<String> fields) {
		this.fields = fields;
	}
	
	@Override
	public void write(JsonWriter out, UserFeedItemContext feedItem) throws IOException {
		out.startObject();
		
		out.name("id").value(feedItem.getFeedItem().getId());
		
		out.name("read").value(feedItem.isRead());
		out.name("title").value(feedItem.getFeedItem().getTitle());
		
		//output the pubDate
		out.name("pubDate");
		Date pubDate = feedItem.getFeedItem().getPubDate();
		if(pubDate == null) {
			out.nullValue();
		} else {
			out.value(pubDate);
		}
		
		out.name("guid").value(feedItem.getFeedItem().getGuid());
		
		//output the feed attribute
		if(this.fields == null || this.fields.contains("feed")) {
			out.name("feed");
			FeedJsonMapper.createSimple().write(out, feedItem.getFeedItem().getFeed());
		}
		
		out.endObject();
	}
	
	public static UserFeedItemContextJsonMapper getDefault() {
		return INSTANCE;
	}
	
	/**
	 * Creates a new builder will all fields included
	 */
	public static Builder buildWithAllFields() {
		return new Builder();
	}
	
	public static class Builder {
		private HashSet<String> fields = new HashSet<String>(ALL_FIELDS);
		
		private Builder() {
		}
		
		/**
		 * Removes all fields from the builder.
		 */
		public Builder clearFields() {
			this.fields.clear();
			return this;
		}
		
		/**
		 * Includes the feed property in the JSON representation.
		 */
		public Builder withFeed() {
			this.fields.add("feed");
			return this;
		}
		
		public UserFeedItemContextJsonMapper build() {
			UserFeedItemContextJsonMapper representation = new UserFeedItemContextJsonMapper(fields);
			return representation;
		}
	}
}