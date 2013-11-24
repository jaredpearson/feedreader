package feedreader.web.rest;

import java.io.IOException;

import common.json.JsonMapper;
import common.json.JsonWriter;
import feedreader.User;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;

class UserFeedContextJsonMapper implements JsonMapper<UserFeedContext> {
	private UserJsonMapper userJsonMapper;
	
	public UserFeedContextJsonMapper(UserJsonMapper userJsonMapper) {
		this.userJsonMapper = userJsonMapper;
	}
	
	public void write(JsonWriter out, UserFeedContext feed) throws IOException {
		assert out != null;
		assert feed != null;
		
		//output the feed context
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
		
		out.name("items");
		out.startArray();
		
		for(UserFeedItemContext feedItem : feed.getItems()) {
			UserFeedItemContextJsonMapper.getDefault().write(out, feedItem);
		}
		
		out.endArray();
		
		out.endObject();
	}
}