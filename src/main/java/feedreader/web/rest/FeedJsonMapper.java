package feedreader.web.rest;

import java.io.IOException;

import common.json.JsonMapper;
import common.json.JsonWriter;
import feedreader.Feed;
import feedreader.User;

class FeedJsonMapper implements JsonMapper<Feed> {
	private UserJsonMapper userJsonMapper;
	
	public FeedJsonMapper(UserJsonMapper userJsonMapper) {
		this.userJsonMapper = userJsonMapper;
	}
	
	public void write(JsonWriter out, Feed feed) throws IOException {

		out.startObject();
		out.name("success").value(true);
		out.name("data");
		out.startObject();
		out.name("id").value(feed.getId());
		out.name("title").value(feed.getTitle());
		out.name("created").value(feed.getCreated());
		out.name("url").value(feed.getUrl());
		
		out.name("createdBy");
		User createdBy = feed.getCreatedBy();
		if(createdBy == null) {
			out.nullValue();
		} else {
			userJsonMapper.write(out, createdBy);
		}
		
		out.endObject();
		out.endObject();
	}
}