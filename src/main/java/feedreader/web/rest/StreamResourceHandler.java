package feedreader.web.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.json.JsonWriter;

import feedreader.FeedReader;
import feedreader.Stream;
import feedreader.UserFeedItemContext;

/**
 * Handler that provides REST-ful services for the {@link Stream}
 * @author jared.pearson
 */
public class StreamResourceHandler {
	
	/**
	 * Gets the aggregate feed, which contains all feed items across multiple RSS feeds. 
	 */
	@RequestHandler(value = "^/v1/stream$", method = Method.GET)
	public void getStream(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader) throws IOException, ServletException {
		Stream feed = feedReader.getStream();
		
		response.setContentType("application/json");
		JsonWriter out = null;
		try {
			out = new JsonWriter(response.getWriter());
			out.startObject();
			out.name("items");
			out.startArray();
			
			//output each feed item with all fields
			UserFeedItemContextJsonMapper representation = UserFeedItemContextJsonMapper.buildWithAllFields().build();
			for(UserFeedItemContext feedItem : feed.getItems()) {
				representation.write(out, feedItem);
			}
			
			out.endArray();
			out.endObject();
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}
	
}
