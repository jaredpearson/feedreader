package feedreader.web.rest.output;

import feedreader.FeedRequestStatus;

/**
 * Resource for the FeedRequest entity
 * @author jared.pearson
 */
public final class FeedRequestResource {
	public final int id;
	public final String url;
	public final FeedRequestStatus status;
	public final Integer feedId;

	public FeedRequestResource(
			final int id,
			final String url,
			final FeedRequestStatus status,
			final Integer feedId) {
		this.id = id;
		this.url = url;
		this.status = status;
		this.feedId = feedId;
	}
}