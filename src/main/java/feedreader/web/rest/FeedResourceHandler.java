package feedreader.web.rest;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import common.persist.EntityManager;
import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.Feed;
import feedreader.FeedReader;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;

/**
 * Handler that provides REST-ful services for the {@link Feed}
 * @author jared.pearson
 */
@Singleton
public class FeedResourceHandler implements ResourceHandler {
	private final ObjectMapper objectMapper;
	
	@Inject
	public FeedResourceHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	/**
	 * Gets the feed corresponding to the request
	 */
	@RequestHandler(value = "^/v1/feed/([0-9]+)$", method = Method.GET)
	public void getFeed(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader, @PathParameter(1) String feedIdValue) throws IOException, ServletException {
		
		//get the requested feed
		int feedId = Integer.valueOf(feedIdValue);
		UserFeedContext feedContext = feedReader.getFeed(feedId);
		
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(request, "v1");
		
		//create the resource models
		final FeedResource feedModel = new FeedResource();
		feedModel.id = feedContext.getId();
		feedModel.title = feedContext.getTitle();
		feedModel.created = feedContext.getCreated();
		feedModel.url = feedContext.getUrl();
		feedModel.items = new FeedItemResource[feedContext.getItems().size()];
		for(int index = 0; index < feedContext.getItems().size(); index++) {
			UserFeedItemContext feedItem = feedContext.getItems().get(index);

			final FeedItemResource feedItemResource = FeedItemResource.fromFeedItem(feedItem, hrefBuilder);
			
			feedModel.items[index] = feedItemResource;
		}
		
		//output the model as JSON
		response.setContentType("application/json");
		Writer out = response.getWriter();
		try {
			objectMapper.writeValue(out, feedModel);
		} finally {
			out.close();
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
	
	public static class MarkReadResponseModel {
		public boolean success;
	}
	
	static class FeedResource {
		public int id;
		public String title;
		public Date created;
		public String url;
		public FeedItemResource[] items;
	}
}