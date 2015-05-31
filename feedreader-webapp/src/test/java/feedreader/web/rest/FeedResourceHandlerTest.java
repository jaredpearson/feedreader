package feedreader.web.rest;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import feedreader.FeedReader;
import feedreader.UserFeedContext;
import feedreader.web.rest.handlers.FeedResourceHandler;
import feedreader.web.rest.output.FeedResource;

public class FeedResourceHandlerTest {
	
	@Test
	public void testGetFeed() throws Exception {
		final Date feedCreatedDate = new Date();
		final UserFeedContext feedContext = mock(UserFeedContext.class);
		when(feedContext.getId()).thenReturn(1);
		when(feedContext.getTitle()).thenReturn("Test Feed");
		when(feedContext.getUrl()).thenReturn("http://example.com/test");
		when(feedContext.getCreated()).thenReturn(feedCreatedDate);
		
		final FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeed(1)).thenReturn(feedContext);
		
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getProtocol()).thenReturn("HTTP/1.1");
		when(request.getPathInfo()).thenReturn("/v1/feed/1");
		
		final HttpServletResponse response = mock(HttpServletResponse.class);
		
		final FeedResourceHandler handler = new FeedResourceHandler();
		final FeedResource resource = handler.getFeed(request, response, feedReader, "1");
		
		assertNotNull("Expected getFeed to not return null", resource);
		assertEquals(1, resource.id);
		assertEquals("Test Feed", resource.title);
		assertEquals("http://example.com/test", resource.url);
		assertEquals("Expected items to be an empty array", 0, resource.items.length);
		assertEquals(feedCreatedDate.getTime(), resource.created.getTime());
	}
	
}
