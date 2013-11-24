package feedreader.web.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import common.json.JsonMapper;
import common.json.JsonWriter;
import feedreader.Feed;
import feedreader.User;

class FeedJsonMapper implements JsonMapper<Feed> {
	private final UserJsonMapper userJsonMapper = UserJsonMapper.getDefaultInstance();
	private Set<String> fields = new HashSet<String>();
	
	public void write(JsonWriter out, Feed feed) throws IOException {
		out.startObject();
		out.name("id").value(feed.getId());
		out.name("title").value(feed.getTitle());
		
		if(fields.contains("created")) {
			out.name("created").value(feed.getCreated());
		}
		
		if(fields.contains("url")) {
			out.name("url").value(feed.getUrl());
		}
		
		if(fields.contains("createdBy")) {
			out.name("createdBy");
			User createdBy = feed.getCreatedBy();
			if(createdBy == null) {
				out.nullValue();
			} else {
				userJsonMapper.write(out, createdBy);
			}
		}
		
		out.endObject();
	}
	
	/**
	 * Creates a mapper which only includes the ID and title fields.
	 */
	public static FeedJsonMapper createSimple() {
		FeedJsonMapper mapper = new FeedJsonMapper();
		mapper.fields = new HashSet<String>();
		mapper.fields.add("id");
		mapper.fields.add("title");
		return mapper;
	}
}