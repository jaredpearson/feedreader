package feedreader.web.rest.output;

import feedreader.FeedRequest;

/**
 * Resource for a page of {@link FeedRequest} instances
 * @author jared.pearson
 */
public class FeedRequestPageResource {
	public final FeedRequestResource[] items;
	public final int total;
	
	public FeedRequestPageResource(FeedRequestResource[] items, int total) {
		this.items = items;
		this.total = total;
	}
}