package feedreader.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Preconditions;

import feedreader.utils.DbUtils;
import feedreader.utils.UserSessionUtils;
import feedreader.utils.UserUtils;

/**
 * End-to-End testing that is independent of the app. The app server must be running
 * and the computer running the tests must be able to access the DB.
 * @author jared.pearson
 */
public class RestEndToEndTest {
	private UserUtils userUtils;
	private UserSessionUtils userSessionUtils;
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Before
	public void setup() {
		final DbUtils dbUtils = new DbUtils();
		this.userUtils = new UserUtils(dbUtils);
		this.userSessionUtils = new UserSessionUtils(dbUtils);
	}
	
	/**
	 * Verifies that an error is thrown when a request is made without the session information  
	 */
	@Test
	public void testGetStreamWithoutAuth() throws Exception {
		final HttpGet getRequest = new HttpGet(createUrl("/services/v1/stream"));
		final HttpClient client = HttpClients.createDefault();
		final HttpResponse response = client.execute(getRequest);
		
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	/**
	 * Verifies that a user with no subscriptions receives an empty stream
	 */
	@Test
	public void testGetEmptyStream() throws Exception {
		final HttpClient client = HttpClients.createDefault();
		final int userId = createUniqueUser();
		final int sessionId = userSessionUtils.createUserSession(userId);
		
		final HttpGet getRequest = new HttpGet(createUrl("/services/v1/stream"));
		getRequest.addHeader("Authorization", "SID " + sessionId);
		
		final HttpResponse response = client.execute(getRequest);
		final JsonRestResponse streamResponse = createFromHttpResponse(response);
		final JsonNode jsonNode = streamResponse.getBodyAsJsonNode();
		
		assertEquals(200, streamResponse.getStatusCode());
		assertTrue("Expected the response to have an \"items\" property", jsonNode.has("items"));
		assertTrue("Expected the \"items\" property to be an array", jsonNode.get("items").isArray());
		assertEquals("Expected the \"items\" property to be an empty array", 0, jsonNode.get("items").size());
	}
	
	/**
	 * Verifies that a user can subscribe to a feed and then see the results in their stream.
	 */
	@Test
	public void testGetFeedAndRequestStream() throws Exception {
		final HttpClient client = HttpClients.createDefault();
		final int userId = createUniqueUser();
		final int sessionId = userSessionUtils.createUserSession(userId);
		final String rssUrl = createUrl("/rssSamples/rss" + (new Random()).nextInt() + ".xml");
		
		// request a new feed be added
		final HttpPost postRequest = new HttpPost(createUrl("/services/v1/feedSubscriptions"));
		postRequest.setEntity(new StringEntity("{\"url\":\"" + rssUrl + "\"}"));
		postRequest.addHeader("Authorization", "SID " + sessionId);
		final JsonRestResponse postFeedSubscriptionResponse = createFromHttpResponse(client.execute(postRequest));
		final JsonNode postFeedSubscriptionResponseNode = postFeedSubscriptionResponse.getBodyAsJsonNode();
		
		assertEquals(200, postFeedSubscriptionResponse.getStatusCode());
		assertTrue("Expected the feedSubscription POST response to have a \"success\" property", postFeedSubscriptionResponseNode.has("success"));
		assertTrue("Expected the feedSubscription POST response to have a true \"success\" property", postFeedSubscriptionResponseNode.get("success").asBoolean());
		assertTrue("Expected the feedSubscription POST response to have a \"feedRequestId\" property", postFeedSubscriptionResponseNode.has("feedRequestId"));
		final int feedRequestId = postFeedSubscriptionResponseNode.get("feedRequestId").asInt();
		
		// request the status of the feed. if the status has not been started, we will keep requesting the status 
		// until it is or the max number of tries is exceeded.
		int retrieveStatusCount = 0;
		FeedRequestStatus feedRequestStatusFromResponse = null;
		do {
			// get the request status for the new feed
			final HttpGet getFeedRequestRequest = new HttpGet(createUrl("/services/v1/feedRequests/" + feedRequestId));
			getFeedRequestRequest.addHeader("Authorization", "SID " + sessionId);
			final JsonRestResponse getFeedRequestResponse = createFromHttpResponse(client.execute(getFeedRequestRequest));
			final JsonNode getFeedRequestResponseNode = getFeedRequestResponse.getBodyAsJsonNode();
			
			assertEquals(200, getFeedRequestResponse.getStatusCode());
			assertFeedRequest(getFeedRequestResponseNode, feedRequestId, rssUrl);
			
			feedRequestStatusFromResponse = FeedRequestStatus.valueOf(getFeedRequestResponseNode.get("status").asText());
			
			retrieveStatusCount++;
			if (feedRequestStatusFromResponse.equals(FeedRequestStatus.NOT_STARTED)) {
				if (retrieveStatusCount > 10) {
					throw new IllegalStateException("Max number of request status tries exceeded.");
				}
				Thread.sleep(2000);
			}
		} while(feedRequestStatusFromResponse.equals(FeedRequestStatus.NOT_STARTED));
		
		// request the stream
		final HttpGet getRequest = new HttpGet(createUrl("/services/v1/stream"));
		getRequest.addHeader("Authorization", "SID " + sessionId);
		
		final JsonRestResponse requestResponse = createFromHttpResponse(client.execute(getRequest));
		final JsonNode jsonNode = requestResponse.getBodyAsJsonNode();

		assertEquals(200, requestResponse.getStatusCode());
		assertTrue("Expected the response to have an \"items\" property", jsonNode.has("items"));
		assertTrue("Expected the \"items\" property to be an array", jsonNode.get("items").isArray());
		assertEquals("Expected the \"items\" property to be an empty array", 4, jsonNode.get("items").size());
		
		JsonNode itemsArrayNode = jsonNode.get("items");
		assertFeedItem(itemsArrayNode.get(0), "Star City", "http://liftoff.msfc.nasa.gov/2003/06/03.html#item573");
		assertFeedItem(itemsArrayNode.get(1), null, "http://liftoff.msfc.nasa.gov/2003/05/30.html#item572");
		assertFeedItem(itemsArrayNode.get(2), "The Engine That Does More", "http://liftoff.msfc.nasa.gov/2003/05/27.html#item571");
		assertFeedItem(itemsArrayNode.get(3), "Astronauts' Dirty Laundry", "http://liftoff.msfc.nasa.gov/2003/05/20.html#item570");
	}

	/**
	 * Verifies that a user can subscribe to multiple feeds and then see the results in their stream. The sample files 
	 * should have their articles' pubDates set so that they are interlaced with each other instead of sequential.
	 */
	@Test
	public void testMultipleFeedAndRequestStream() throws Exception {
		final HttpClient client = HttpClients.createDefault();
		final int userId = createUniqueUser();
		final int sessionId = userSessionUtils.createUserSession(userId);
		
		// request the first RSS
		final String rssSample1Url = createUrl("/rssSamples/sample1-" + (new Random()).nextInt() + ".xml");
		requestFeedAndWait(client, sessionId, rssSample1Url);
		
		// request the second RSS
		final String rssSample2Url = createUrl("/rssSamples/sample2-" + (new Random()).nextInt() + ".xml");
		requestFeedAndWait(client, sessionId, rssSample2Url);
		
		// request the stream
		final HttpGet getRequest = new HttpGet(createUrl("/services/v1/stream"));
		getRequest.addHeader("Authorization", "SID " + sessionId);
		
		final JsonRestResponse requestResponse = createFromHttpResponse(client.execute(getRequest));
		final JsonNode jsonNode = requestResponse.getBodyAsJsonNode();

		assertEquals(200, requestResponse.getStatusCode());
		assertTrue("Expected the response to have an \"items\" property", jsonNode.has("items"));
		assertTrue("Expected the \"items\" property to be an array", jsonNode.get("items").isArray());
		assertEquals("Expected the \"items\" property to be an empty array", 8, jsonNode.get("items").size());
		
		JsonNode itemsArrayNode = jsonNode.get("items");
		assertFeedItem(itemsArrayNode.get(0), "Sample 2 Item 4", "http://example.com/news/2/4");
		assertFeedItem(itemsArrayNode.get(1), "Sample 1 Item 4", "http://example.com/news/1/4");
		assertFeedItem(itemsArrayNode.get(2), "Sample 2 Item 3", "http://example.com/news/2/3");
		assertFeedItem(itemsArrayNode.get(3), null, "http://example.com/news/1/3");
		assertFeedItem(itemsArrayNode.get(4), "Sample 1 Item 2", "http://example.com/news/1/2");
		assertFeedItem(itemsArrayNode.get(5), "Sample 1 Item 1", "http://example.com/news/1/1");
		assertFeedItem(itemsArrayNode.get(6), "Sample 2 Item 2", "http://example.com/news/2/2");
		assertFeedItem(itemsArrayNode.get(7), "Sample 2 Item 1", "http://example.com/news/2/1");
	}
	
	/**
	 * Verifies that a user can view a feed.
	 */
	@Test
	public void testViewFeed() throws Exception {
		final HttpClient client = HttpClients.createDefault();
		final int userId = createUniqueUser();
		final int sessionId = userSessionUtils.createUserSession(userId);
		
		// request the RSS feed
		final String rssSample1Url = createUrl("/rssSamples/sample1-" + (new Random()).nextInt() + ".xml");
		final FeedRequest request = requestFeedAndWait(client, sessionId, rssSample1Url);
		
		// request the feed
		final HttpGet getRequest = new HttpGet(createUrl("/services/v1/feeds/" + request.getFeedId()));
		getRequest.addHeader("Authorization", "SID " + sessionId);
		
		final JsonRestResponse requestResponse = createFromHttpResponse(client.execute(getRequest));
		final JsonNode jsonNode = requestResponse.getBodyAsJsonNode();

		assertEquals(200, requestResponse.getStatusCode());
		assertTrue("Expected the response to have an \"items\" property", jsonNode.has("items"));
		assertTrue("Expected the \"items\" property to be an array", jsonNode.get("items").isArray());
		assertEquals("Expected the \"items\" property to be an empty array", 4, jsonNode.get("items").size());
		
		JsonNode itemsArrayNode = jsonNode.get("items");
		assertFeedItem(itemsArrayNode.get(1), null, "http://example.com/news/1/3");
		assertFeedItem(itemsArrayNode.get(2), "Sample 1 Item 2", "http://example.com/news/1/2");
		assertFeedItem(itemsArrayNode.get(3), "Sample 1 Item 1", "http://example.com/news/1/1");
	}

	/**
	 * Verifies that a user can mark a feed item read
	 */
	@Test
	public void testMarkRead() throws Exception {
		final HttpClient client = HttpClients.createDefault();
		final int userId = createUniqueUser();
		final int sessionId = userSessionUtils.createUserSession(userId);
		final String expectedFeedItemTitle = "Sample 1 Item 4";
		final String expectedFeedItemGuid = "http://example.com/news/1/4";
		
		// request the RSS feed
		final String rssSample1Url = createUrl("/rssSamples/sample1-" + (new Random()).nextInt() + ".xml");
		final FeedRequest request = requestFeedAndWait(client, sessionId, rssSample1Url);
		
		// request the feed
		final HttpGet getFeedRequest = new HttpGet(createUrl("/services/v1/feeds/" + request.getFeedId()));
		getFeedRequest.addHeader("Authorization", "SID " + sessionId);
		
		final JsonRestResponse getFeedRequestResponse = createFromHttpResponse(client.execute(getFeedRequest));
		final JsonNode feedNode = getFeedRequestResponse.getBodyAsJsonNode();

		assertEquals(200, getFeedRequestResponse.getStatusCode());
		assertTrue("Expected the response to have an \"items\" property", feedNode.has("items"));
		assertTrue("Expected the \"items\" property to be an array", feedNode.get("items").isArray());
		assertEquals("Expected the \"items\" property to be an array of 4 items", 4, feedNode.get("items").size());
		
		final JsonNode itemsArrayNode = feedNode.get("items");
		final JsonNode feedItemNode = itemsArrayNode.get(0);
		assertFeedItem(feedItemNode, expectedFeedItemTitle, expectedFeedItemGuid, false);
		final int feedItemId = feedItemNode.get("id").asInt();
		
		// patch the request so that the read status is now true
		final HttpPatch patchRequest = new HttpPatch(createUrl("/services/v1/feedItems/" + feedItemId));
		patchRequest.addHeader("Authorization", "SID " + sessionId);
		patchRequest.setEntity(new StringEntity("{\"read\":true}"));
		
		final JsonRestResponse patchResponse = createFromHttpResponse(client.execute(patchRequest));
		
		assertEquals(200, patchResponse.getStatusCode());
		assertFeedItem(patchResponse.getBodyAsJsonNode(), expectedFeedItemTitle, expectedFeedItemGuid, true);
		
		// reload the request so that we know that the read status has been updated
		final HttpGet feedItemRequest = new HttpGet(createUrl("/services/v1/feedItems/" + feedItemId));
		feedItemRequest.addHeader("Authorization", "SID " + sessionId);
		
		final JsonRestResponse feedItemResponse = createFromHttpResponse(client.execute(patchRequest));
		
		assertEquals(200, feedItemResponse.getStatusCode());
		assertFeedItem(feedItemResponse.getBodyAsJsonNode(), expectedFeedItemTitle, expectedFeedItemGuid, true);
	}
	
	/**
	 * Requests a feed to be added at the given RSS url and wait until it is in a status other than NOT_STARTED
	 * @return the request after it has been started
	 */
	private FeedRequest requestFeedAndWait(HttpClient client, int sessionId, String rssUrl) throws Exception {
		// request a new feed be added
		final HttpPost postRequest = new HttpPost(createUrl("/services/v1/feedSubscriptions"));
		postRequest.setEntity(new StringEntity("{\"url\":\"" + rssUrl + "\"}"));
		postRequest.addHeader("Authorization", "SID " + sessionId);
		final JsonRestResponse postFeedSubscriptionResponse = createFromHttpResponse(client.execute(postRequest));
		final JsonNode postFeedSubscriptionResponseNode = postFeedSubscriptionResponse.getBodyAsJsonNode();
		
		assertEquals(200, postFeedSubscriptionResponse.getStatusCode());
		assertTrue("Expected the feedSubscription POST response to have a \"success\" property", postFeedSubscriptionResponseNode.has("success"));
		assertTrue("Expected the feedSubscription POST response to have a true \"success\" property", postFeedSubscriptionResponseNode.get("success").asBoolean());
		assertTrue("Expected the feedSubscription POST response to have a \"feedRequestId\" property", postFeedSubscriptionResponseNode.has("feedRequestId"));
		final int feedRequestId = postFeedSubscriptionResponseNode.get("feedRequestId").asInt();
		
		// request the status of the feed. if the status has not been started, we will keep requesting the status 
		// until it is or the max number of tries is exceeded.
		int retrieveStatusCount = 0;
		FeedRequest feedRequest = null;
		do {
			// get the request status for the new feed
			final HttpGet getFeedRequestRequest = new HttpGet(createUrl("/services/v1/feedRequests/" + feedRequestId));
			getFeedRequestRequest.addHeader("Authorization", "SID " + sessionId);
			final JsonRestResponse getFeedRequestResponse = createFromHttpResponse(client.execute(getFeedRequestRequest));
			final JsonNode getFeedRequestResponseNode = getFeedRequestResponse.getBodyAsJsonNode();
			
			assertEquals(200, getFeedRequestResponse.getStatusCode());
			assertFeedRequest(getFeedRequestResponseNode, feedRequestId, rssUrl);
			
			feedRequest = objectMapper.readValue(getFeedRequestResponseNode, FeedRequest.class);
			
			retrieveStatusCount++;
			if (feedRequest.isNotStarted()) {
				if (retrieveStatusCount > 10) {
					throw new IllegalStateException("Max number of request status tries exceeded.");
				}
				Thread.sleep(2000);
			}
		} while(feedRequest.isNotStarted());
		
		return feedRequest;
	}
	
	/**
	 * Builds a response from the HTTP response.
	 */
	private JsonRestResponse createFromHttpResponse(@Nonnull final HttpResponse response) throws IOException {
		Preconditions.checkArgument(response != null, "response should not be null");

		final ContentType postResponseContentType = ContentType.get(response.getEntity());
		if (postResponseContentType == null) {
			throw new IllegalStateException("Content-Type header should always be returned.");
		}
		if (!"application/json".equals(postResponseContentType.getMimeType())) {
			throw new IllegalStateException("Expected Content-Type header to always return \"application/json\": " + postResponseContentType.toString());
		}
		
		final String postResponseBody = EntityUtils.toString(response.getEntity(), postResponseContentType.getCharset());
		final JsonNode postResponseNode = objectMapper.readTree(postResponseBody);
		
		return new JsonRestResponse(response.getStatusLine().getStatusCode(), postResponseNode);
	}
	
	/**
	 * Asserts that the feed item node has the given title and guid.
	 * @param feedItemNode the node to verify
	 * @param title the title of the node or null if the title should not be specified
	 * @param guid the guid of the node or null if the guid should not be specified
	 */
	private static void assertFeedItem(@Nonnull JsonNode feedItemNode, String title, String guid) {
		assertFeedItem(feedItemNode, title, guid, false);
	}

	/**
	 * Asserts that the feed item node has the given title and guid.
	 * @param feedItemNode the node to verify
	 * @param title the title of the node or null if the title should not be specified
	 * @param guid the guid of the node or null if the guid should not be specified
	 * @param readStatus the read status of the node
	 */
	private static void assertFeedItem(@Nonnull JsonNode feedItemNode, String title, String guid, boolean readStatus) {
		assertTrue("Expected feedItem to have a \"title\" property", feedItemNode.has("title"));
		if (title == null) {
			assertTrue("Unexpected value for feedItem \"title\" property: " + feedItemNode.get("title"), feedItemNode.get("title").isNull());
		} else {
			assertEquals("Unexpected value for feedItem \"title\" property", title, feedItemNode.get("title").asText());
		}
		
		assertTrue("Expected feedItem to have a \"guid\" property", feedItemNode.has("guid"));
		if (guid == null) {
			assertNull("Unexpected value for feedItem \"guid\" property: " + feedItemNode.get("guid"), feedItemNode.get("guid").isNull());
		} else {
			assertEquals("Unexpected value for feedItem \"guid\" property", guid, feedItemNode.get("guid").asText());
		}
		
		assertTrue("Expected feedItem to have a \"read\" property", feedItemNode.has("read"));
		assertEquals("Unexpected value for feedItem \"read\" property", readStatus, feedItemNode.get("read").asBoolean());
	}

	/**
	 * Asserts thats the feed request node has the given property values.
	 */
	private void assertFeedRequest(final JsonNode feedRequestNode, final int feedRequestId, final String rssUrl) {
		Preconditions.checkArgument(feedRequestNode != null, "feedRequestNode should not be null");
		
		assertTrue("Expected the feedRequest to have a \"id\" property", feedRequestNode.has("id"));
		assertEquals("Expected the feedRequest \"id\" property to have the same feed request ID as requested", feedRequestId, feedRequestNode.get("id").asInt());
		
		assertTrue("Expected the feedRequest to have a \"url\" property", feedRequestNode.has("url"));
		assertEquals("Expected the feedRequest \"url\" property to have the same url as requested", rssUrl, feedRequestNode.get("url").asText());
		
		assertTrue("Expected the feedRequest to have a \"status\" property", feedRequestNode.has("status"));
		final FeedRequestStatus feedRequestStatusFromResponse = FeedRequestStatus.valueOf(feedRequestNode.get("status").asText());
		assertNotNull("Expected the feedRequest \"status\" property to have a valid value.\nactual: " + feedRequestNode.get("status").asText(), feedRequestStatusFromResponse);
	}

	/**
	 * Creates a new unique user for use in the test
	 * @return the ID of the new user
	 */
	private int createUniqueUser() throws SQLException {
		return userUtils.createUser("test@test" + (new Random()).nextInt() + ".com");
	}
	
	/**
	 * Creates a URL for the test server given the specified path.
	 */
	private static String createUrl(final String path) {
		return "http://localhost:8080" + path;
	}
	
	/**
	 * Response from making a request.
	 * @author jared.pearson
	 */
	private static class JsonRestResponse {
		private final int statusCode;
		private final JsonNode bodyNode;
		
		public JsonRestResponse(final int statusCode, @Nonnull final JsonNode bodyNode) {
			Preconditions.checkArgument(bodyNode != null, "bodyNode should not be null");
			this.statusCode = statusCode;
			this.bodyNode = bodyNode;
		}
		
		/**
		 * Gets the status code returned by the server.
		 */
		public int getStatusCode() {
			return statusCode;
		}
		
		/**
		 * Gets the body of the request as a JSON node.
		 */
		public JsonNode getBodyAsJsonNode() {
			return bodyNode;
		}
	}
	
	enum FeedRequestStatus {
		NOT_STARTED,
		FINISHED,
		ERROR
	}
	
	static class FeedRequest {
		private int id; 
		private FeedRequestStatus status;
		private Integer feedId;
		private String url;
		
		public FeedRequest() {
		}
		
		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}
		
		public FeedRequestStatus getStatus() {
			return status;
		}
		
		public void setStatus(FeedRequestStatus status) {
			this.status = status;
		}
		
		public Integer getFeedId() {
			return feedId;
		}
		
		public void setFeedId(Integer feedId) {
			this.feedId = feedId;
		}
		
		public String getUrl() {
			return url;
		}
		
		public void setUrl(String url) {
			this.url = url;
		}
		
		public boolean isNotStarted() {
			return FeedRequestStatus.NOT_STARTED.equals(this.status);
		}
		
	}
	
}
