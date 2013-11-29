package feedreader.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import feedreader.FeedReader;
import feedreader.FeedRequest;

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
		
		final StringWriter writer = new StringWriter();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				writer.write(b);
			}
		});
		
		FeedSubscriptionResourceHandler handler = new FeedSubscriptionResourceHandler(new ObjectMapper());
		handler.createSubscription(request, response, feedReader);
		
		String expected = "{\"success\":true,\"feedRequestId\":1}";
		assertEquals(expected, writer.toString());
	}

}
