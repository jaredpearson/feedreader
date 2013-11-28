package feedreader.web.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import common.json.JsonWriter;
import common.json.JsonWriterFactory;
import common.persist.EntityManager;
import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import feedreader.Feed;
import feedreader.FeedReader;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;

/**
 * Handler that provides REST-ful services for the {@link Feed}
 * @author jared.pearson
 */
public class FeedResourceHandler {
	private final ObjectMapper objectMapper;
	private final JsonWriterFactory jsonWriterFactory;
	
	public FeedResourceHandler() {
		this.objectMapper = new ObjectMapper();
		this.jsonWriterFactory = new JsonWriterFactory();
	}
	
	/**
	 * Gets the feed corresponding to the request
	 */
	@RequestHandler(value = "^/v1/feed/([0-9]+)$", method = Method.GET)
	public void getFeed(HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedIdValue) throws IOException, ServletException {
		
		//get the requested feed
		int feedId = Integer.valueOf(feedIdValue);
		UserFeedContext feedContext = feedReader.getFeed(feedId);
		
		//create the response model
		GetFeedResponseModel responseModel = new GetFeedResponseModel();
		responseModel.success = true;
		
		response.setContentType("application/json");
		JsonWriter out = null;
		try {
			out = jsonWriterFactory.createWithWriter(response.getWriter());

			out.startObject();
			out.name("success").value(true);
			out.name("data");
			
			UserFeedContextJsonMapper mapper = new UserFeedContextJsonMapper(new UserJsonMapper());
			mapper.write(out, feedContext);
			
			out.endObject();
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}
	
	@RequestHandler(value = "^/v1/feed/([0-9]+)/item/([0-9]+)/read$", method = Method.POST)
	public void markFeedItemRead(HttpServletResponse response, 
			EntityManager entityManager, FeedReader feedReader, @PathParameter(1) String feedIdValue, 
			@PathParameter(2) String feedItemIdValue) throws IOException, ServletException {
		
		//get the requested feed
		int feedId = Integer.valueOf(feedIdValue);
		UserFeedContext feedContext = feedReader.getFeed(feedId);
		
		//get the request feed item
		int feedItemId = Integer.valueOf(feedItemIdValue);
		UserFeedItemContext feedItem = feedContext.getItemWithFeedItemId(feedItemId);
		
		if(feedItem == null) {
			response.sendError(404);
			return;
		}
		
		//mark the item read
		feedItem.setRead(true);
		
		//save the item
		entityManager.persist(feedItem);
		
		//create the response model
		MarkReadResponseModel model = new MarkReadResponseModel();
		model.success = true;
		
		//output to the response
		response.setContentType("application/json");
		Writer out = response.getWriter();
		try {
			objectMapper.writeValue(out, model);
		} finally {
			out.close();
		}
	}
	
	public static class GetFeedResponseModel {
		public boolean success;
		public Object data;
	}
	
	public static class MarkReadResponseModel {
		public boolean success;
	}
}