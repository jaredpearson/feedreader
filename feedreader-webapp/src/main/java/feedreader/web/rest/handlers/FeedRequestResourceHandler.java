package feedreader.web.rest.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedReader;
import feedreader.FeedRequest;
import feedreader.FeedRequestPage;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.web.rest.DeserializerUtil;
import feedreader.web.rest.input.FeedRequestInputResource;
import feedreader.web.rest.output.FeedRequestPageResource;
import feedreader.web.rest.output.FeedRequestResource;

/**
 * Resource handler for FeedRequest handlers
 * @author jared.pearson
 */
@Singleton
public class FeedRequestResourceHandler implements ResourceHandler {
	private final DataSource dataSource;
	private final FeedRequestEntityHandler feedRequestResourceHandler;
	private final DeserializerUtil deserializerUtil;
	
	@Inject
	public FeedRequestResourceHandler(DataSource dataSource, FeedRequestEntityHandler feedRequestResourceHandler, DeserializerUtil deserializerUtil) {
		this.dataSource = dataSource;
		this.feedRequestResourceHandler = feedRequestResourceHandler;
		this.deserializerUtil = deserializerUtil;
	}

	@RequestHandler(value="^/v1/feedRequests$", method=Method.POST)
	public FeedRequestResource createRequest(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader) 
			throws IOException, ServletException {
		
		//TODO: we assume that only content of application/json is specified
		final FeedRequestInputResource input = deserializerUtil.deserializeFromRequestBodyAsJson(request, FeedRequestInputResource.class);
		if (input == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing input body");
			return null;
		}
		if (input.url == null || input.url.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL must be specified");
			return null;
		}
		
		final int feedRequestId = feedReader.addFeedFromUrl(input.url);

		final FeedRequest feedRequest;
		try {
			final Connection cnn = dataSource.getConnection(); 
			feedRequest = feedRequestResourceHandler.findFeedRequestById(cnn, feedRequestId);
		} catch (SQLException exc) {
			throw new RuntimeException(exc);
		}
		return createFeedRequestResource(feedRequest);
	}

	/**
	 * Gets the feed corresponding to the request
	 */
	@RequestHandler(value = "^/v1/feedRequests/([0-9]+)$", method = Method.GET)
	public FeedRequestResource getFeedRequest(HttpServletRequest request, HttpServletResponse response, @PathParameter(1) String feedRequestIdValue) throws IOException {
		
		if (feedRequestIdValue == null) {
			response.sendError(400);
			return null;
		}
		final int feedRequestId;
		try {
			feedRequestId = Integer.valueOf(feedRequestIdValue);
		} catch(NumberFormatException exc) {
			response.sendError(400);
			return null;
		}
		
		final FeedRequest feedRequest;
		try {
			final Connection cnn = dataSource.getConnection(); 
			feedRequest = feedRequestResourceHandler.findFeedRequestById(cnn, feedRequestId);
		} catch (SQLException exc) {
			throw new RuntimeException(exc);
		}
		
		if (feedRequest == null) {
			response.sendError(404);
			return null;
		}
		
		return createFeedRequestResource(feedRequest);
	}

	/**
	 * Gets the requests that have been made by the current user
	 */
	@RequestHandler(value = "^/v1/feedRequests$", method = Method.GET)
	public FeedRequestPageResource getFeedRequests(HttpServletRequest request, HttpServletResponse response, FeedReader feedReader) throws IOException {
		
		final FeedRequestPage page = feedReader.getFeedRequestsForCurrentUser(0, 20);
		
		final FeedRequestResource[] itemResources = new FeedRequestResource[page.getItems().size()];
		for (int index = 0; index < page.getItems().size(); index++) {
			final FeedRequest feedRequest = page.getItems().get(index);
			final FeedRequestResource feedRequestResource = createFeedRequestResource(feedRequest);
			itemResources[index] = feedRequestResource;
		}
		
		return new FeedRequestPageResource(itemResources, page.getTotal());
	}

	private FeedRequestResource createFeedRequestResource(final @Nonnull FeedRequest feedRequest) {
		final FeedRequestResource responseModel = new FeedRequestResource();
		responseModel.id = feedRequest.getId();
		responseModel.url = feedRequest.getUrl();
		responseModel.status = feedRequest.getStatus();
		responseModel.feedId = feedRequest.getFeedId();
		return responseModel;
	}
}
