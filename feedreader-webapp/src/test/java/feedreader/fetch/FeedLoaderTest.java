package feedreader.fetch;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import feedreader.Feed;
import feedreader.fetch.FeedLoader;

public class FeedLoaderTest {
	
	@Test
	public void testLoadFromUrl() throws Exception {
		URL sampleUrl = Thread.currentThread().getContextClassLoader().getResource("rss2sample.xml");
		
		FeedLoader feedLoader = new FeedLoader();
		Feed feed = feedLoader.loadFromUrl(sampleUrl.toString());
		
		assertEquals("Liftoff News", feed.getTitle());
		assertEquals(4, feed.getItems().size());
	}
	
}
