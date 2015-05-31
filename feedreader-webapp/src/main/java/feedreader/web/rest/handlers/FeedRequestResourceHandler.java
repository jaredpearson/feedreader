package feedreader.web.rest.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import common.web.rest.Method;
import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.FeedRequest;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.web.rest.output.FeedRequestResource;

/**
 * Resource handler for FeedRequest handlers
 * @author jared.pearson
 */
@Singleton
public class FeedRequestResourceHandler implements ResourceHandler {
	private final DataSource dataSource;
	private final FeedRequestEntityHandler feedRequestResourceHandler;
	
	@Inject
	public FeedRequestResourceHandler(DataSource dataSource, FeedRequestEntityHandler feedRequestResourceHandler) {
		this.dataSource = dataSource;
		this.feedRequestResourceHandler = feedRequestResourceHandler;
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
		
		final FeedRequestResource responseModel = new FeedRequestResource();
		responseModel.id = feedRequest.getId();
		responseModel.url = feedRequest.getUrl();
		responseModel.status = feedRequest.getStatus();
		responseModel.feedId = feedRequest.getFeedId();
		return responseModel;
	}
}
