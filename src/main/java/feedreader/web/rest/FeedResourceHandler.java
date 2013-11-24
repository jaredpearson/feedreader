package feedreader.web.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.json.JsonWriter;
import common.persist.EntityManager;
import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import feedreader.Feed;
import feedreader.FeedReader;
import feedreader.FeedRequest;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;

/**
 * Handler that provides REST-ful services for the {@link Feed}
 * @author jared.pearson
 */
public class FeedResourceHandler {
	
	/**
	 * Gets the feed corresponding to the request
	 */
	@RequestHandler(value = "^/v1/feed/([0-9]+)$", method = Method.GET)
	public void getFeed(HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedIdValue) throws IOException, ServletException {
		
		//get the requested feed
		int feedId = Integer.valueOf(feedIdValue);
		UserFeedContext feedContext = feedReader.getFeed(feedId);
		
		response.setContentType("application/json");
		JsonWriter out = null;
		try {
			out = new JsonWriter(response.getWriter());

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
		
		response.setContentType("application/json");
		JsonWriter out = null;
		try {
			out = new JsonWriter(response.getWriter());
			out.startObject();
			out.name("success").value(true);
			out.endObject();
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * Creates a feed from the given request
	 */
	@RequestHandler(value = "^/v1/feed$", method = Method.POST)
	public void createFeed(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader) throws IOException, ServletException {
		String url = request.getParameter("url");
		if(url == null || url.trim().length() == 0) {
			throw new IllegalArgumentException();
		}
		
		FeedRequest feedRequest = feedReader.addFeedFromUrl(url);
		
		//output the success
		response.setContentType("application/json");
		JsonWriter out = null;
		try {
			out = new JsonWriter(response.getWriter());
			out.startObject();
			out.name("success").value(true);
			out.name("feedRequestId").value(feedRequest.getId());
			out.endObject();
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}
}