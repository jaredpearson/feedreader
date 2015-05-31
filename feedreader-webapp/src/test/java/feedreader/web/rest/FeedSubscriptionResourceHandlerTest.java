package feedreader.web.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import feedreader.FeedReader;
import feedreader.FeedRequest;
import feedreader.web.rest.handlers.FeedSubscriptionResourceHandler;
import feedreader.web.rest.handlers.FeedSubscriptionResourceHandler.CreateFeedSubscriptionResource;

public class FeedSubscriptionResourceHandlerTest {
	
	@Test
	public void testCreateSubscription() throws Exception {
		FeedRequest feedRequest = new FeedRequest();
		feedRequest.setId(1);
		
		final FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.addFeedFromUrl("http://test.com")).thenReturn(1);
		
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feedSubscription");
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"url\":\"http://test.com\"}")));

		final HttpServletResponse response = mock(HttpServletResponse.class);
		
		final DeserializerUtil deserializerUtil = new DeserializerUtil(new ObjectMapper());
		
		FeedSubscriptionResourceHandler handler = new FeedSubscriptionResourceHandler(deserializerUtil);
		CreateFeedSubscriptionResource resource = handler.createSubscription(request, response, feedReader);
		
		assertTrue(resource.success);
		assertEquals(1, resource.feedRequestId);
	}

}
