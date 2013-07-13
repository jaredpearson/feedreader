package feedreader.web.rest;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import common.persist.EntityManager;
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
		
		EntityManager entityManager = mock(EntityManager.class);
		when(entityManager.get(eq(Feed.class), eq(1))).thenReturn(feed);
		
		FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeed(1)).thenReturn(feedContext);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feed/1");
		
		StringWriter writer = new StringWriter();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		FeedResourceHandler handler = new FeedResourceHandler();
		handler.getFeed(request, response, feedReader);
		
		String expected = "{\"success\":true,\"data\":{\"id\":1,\"title\":\"Test Feed\",\"created\":null,\"url\":null,\"createdBy\":null,\"items\":[]}}";
		assertEquals(expected, writer.toString());
	}
	
	@Test
	public void testCreateFeed() throws Exception {
		UserFeedContext feedContext = mock(UserFeedContext.class);
		when(feedContext.getId()).thenReturn(1);
		when(feedContext.getTitle()).thenReturn("Test Feed");
		
		FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.addFeedFromUrl(eq("http://test.com"))).thenReturn(feedContext);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feed");
		when(request.getParameter(eq("url"))).thenReturn("http://test.com");
		
		StringWriter writer = new StringWriter();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		FeedResourceHandler handler = new FeedResourceHandler();
		handler.createFeed(request, response, feedReader);
		
		String expected = "{\"success\":true,\"id\":1}";
		assertEquals(expected, writer.toString());
	}

	@Test
	public void testMarkRead() throws Exception {
		UserFeedItemContext feedItemContext = mock(UserFeedItemContext.class);
		
		UserFeedContext feedContext = mock(UserFeedContext.class);
		when(feedContext.getId()).thenReturn(1);
		when(feedContext.getTitle()).thenReturn("Test Feed");
		when(feedContext.getItemWithFeedItemId(1)).thenReturn(feedItemContext);
		
		EntityManager entityManager = mock(EntityManager.class);
		
		FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeed(eq(1))).thenReturn(feedContext);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feed/1/item/1/read");
		
		StringWriter writer = new StringWriter();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		FeedResourceHandler handler = new FeedResourceHandler();
		handler.markFeedItemRead(request, response, entityManager, feedReader);
		
		verify(entityManager).persist(eq(feedItemContext));
		
		String expected = "{\"success\":true}";
		assertEquals(expected, writer.toString());
	}
}
