package feedreader.web.rest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import feedreader.FeedReader;
import feedreader.UserFeedItemContext;

public class FeedItemResourceHandlerTest {
	
	@Test
	public void testUpdateReadStatusWithPatch() throws Exception {
		final UserFeedItemContext feedItemContext = mock(UserFeedItemContext.class);
		
		final FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeedItem(eq(1))).thenReturn(feedItemContext);
		
		final BufferedReader bufferedReader = new BufferedReader(new StringReader("{\"read\":true}"));
		
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getReader()).thenReturn(bufferedReader);
		when(request.getPathInfo()).thenReturn("/v1/feedItem/1");
		when(request.getProtocol()).thenReturn("");
		
		final StringWriter writer = new StringWriter();
		final HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		final FeedItemResourceHandler handler = new FeedItemResourceHandler(new ObjectMapper());
		final FeedItemResource resource = handler.patchFeedItem(request, response, feedReader, "1");
		
		verify(feedReader).markReadStatus(eq(1), eq(true));
		verify(response, never()).sendError(anyInt());
		assertNotNull(resource);
	}

	@Test
	public void testUpdateReadStatusWithPatchWhenNoBody() throws Exception {
		final UserFeedItemContext feedItemContext = mock(UserFeedItemContext.class);
		
		final FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.getFeedItem(eq(1))).thenReturn(feedItemContext);
		
		// send in an empty body
		final BufferedReader bufferedReader = new BufferedReader(new StringReader(""));
		
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getReader()).thenReturn(bufferedReader);
		when(request.getPathInfo()).thenReturn("/v1/feedItem/1");
		when(request.getProtocol()).thenReturn("");
		
		final StringWriter writer = new StringWriter();
		final HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		
		final FeedItemResourceHandler handler = new FeedItemResourceHandler(new ObjectMapper());
		final FeedItemResource resource = handler.patchFeedItem(request, response, feedReader, "1");
		
		verify(feedReader, never()).markReadStatus(anyInt(), anyBoolean());
		verify(response, times(1)).sendError(eq(400), eq("Missing input body"));
		assertNull("Expected the response to be null since the request body was empty", resource);
	}

}
