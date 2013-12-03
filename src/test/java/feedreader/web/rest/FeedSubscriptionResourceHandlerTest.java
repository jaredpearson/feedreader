package feedreader.web.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import feedreader.FeedReader;
import feedreader.FeedRequest;
import feedreader.web.rest.FeedSubscriptionResourceHandler.CreateFeedSubscriptionResponse;

public class FeedSubscriptionResourceHandlerTest {
	
	@Test
	public void testCreateSubscription() throws Exception {
		FeedRequest feedRequest = new FeedRequest();
		feedRequest.setId(1);
		
		FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.addFeedFromUrl("http://test.com")).thenReturn(feedRequest);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feedSubscription");
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"url\":\"http://test.com\"}")));
		
		FeedSubscriptionResourceHandler handler = new FeedSubscriptionResourceHandler(new ObjectMapper());
		CreateFeedSubscriptionResponse response = handler.createSubscription(request, feedReader);
		
		assertTrue(response.success);
		assertEquals(1, response.feedRequestId);
	}

}
