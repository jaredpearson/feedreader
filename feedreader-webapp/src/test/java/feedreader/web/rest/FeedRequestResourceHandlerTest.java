package feedreader.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import feedreader.FeedReader;
import feedreader.FeedRequest;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.web.rest.handlers.FeedRequestResourceHandler;
import feedreader.web.rest.output.FeedRequestResource;

/**
 * Tests for the {@link FeedRequestResourceHandler}
 * @author jared.pearson
 */
public class FeedRequestResourceHandlerTest {

	/**
	 * Verifies that the createRequest method is able to create the request
	 */
	@Test
	public void testCreateRequest() throws Exception {
		FeedRequest feedRequest = new FeedRequest();
		feedRequest.setId(1);
		final String expectedUrl = "http://test.com";
		feedRequest.setUrl(expectedUrl);
		
		final FeedReader feedReader = mock(FeedReader.class);
		when(feedReader.addFeedFromUrl(expectedUrl)).thenReturn(1);
		
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/v1/feedRequests");
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"url\":\"" + expectedUrl + "\"}")));

		final HttpServletResponse response = mock(HttpServletResponse.class);
		
		final Connection cnn = mock(Connection.class);
		final DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenReturn(cnn);
		
		final FeedRequestEntityHandler feedRequestEntityHandler = mock(FeedRequestEntityHandler.class);
		when(feedRequestEntityHandler.findFeedRequestById(any(Connection.class), eq(1))).thenReturn(feedRequest);
		
		final DeserializerUtil deserializerUtil = new DeserializerUtil(new ObjectMapper());
		
		FeedRequestResourceHandler handler = new FeedRequestResourceHandler(dataSource, feedRequestEntityHandler, deserializerUtil);
		FeedRequestResource resource = handler.createRequest(request, response, feedReader);
		
		assertEquals(expectedUrl, resource.url);
		assertEquals(1, resource.id);
	}
	
}
