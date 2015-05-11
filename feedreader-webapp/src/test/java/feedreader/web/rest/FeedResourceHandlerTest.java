package feedreader.web.rest;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import feedreader.Feed;
import feedreader.FeedReader;
import feedreader.UserFeedContext;
import feedreader.UserFeedItemContext;

public class FeedResourceHandlerTest {
	
	@Test
	public void testGetFeed() throws Exception {
		Feed feed = new Feed();
		feed.setId(1);
		feed.setTitle("Test Feed");
		
		UserFeedContext feedContext = mock(UserFeedContext.class);
		when(feedContext.getId()).thenReturn(1);
		when(feedContext.getTitle()).thenReturn("Test Feed");
		
		FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeed(1)).thenReturn(feedContext);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getProtocol()).thenReturn("HTTP/1.1");
		when(request.getPathInfo()).thenReturn("/v1/feed/1");
		
		StringWriter writer = new StringWriter();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		FeedResourceHandler handler = new FeedResourceHandler(new ObjectMapper());
		handler.getFeed(request, response, feedReader, "1");
		
		String expected = "{\"id\":1,\"title\":\"Test Feed\",\"created\":null,\"url\":null,\"items\":[]}";
		assertEquals(expected, writer.toString());
	}
	
	@Test
	public void testMarkRead() throws Exception {
		UserFeedItemContext feedItemContext = mock(UserFeedItemContext.class);
		
		UserFeedContext feedContext = mock(UserFeedContext.class);
		when(feedContext.getId()).thenReturn(1);
		when(feedContext.getTitle()).thenReturn("Test Feed");
		when(feedContext.getItemWithFeedItemId(1)).thenReturn(feedItemContext);
		
		FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeed(eq(1))).thenReturn(feedContext);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feed/1/item/1/read");
		
		StringWriter writer = new StringWriter();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		FeedResourceHandler handler = new FeedResourceHandler(new ObjectMapper());
		handler.markFeedItemRead(response, feedReader, "1", "1");
		
		verify(feedReader).markReadStatus(eq(1), eq(true));
		
		String expected = "{\"success\":true}";
		assertEquals(expected, writer.toString());
	}
}
