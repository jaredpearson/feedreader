package feedreader.web.rest.output;

import feedreader.FeedRequestStatus;

/**
 * Resource for the FeedRequest entity
 * @author jared.pearson
 */
public final class FeedRequestResource {
	public int id;
	public String url;
	public FeedRequestStatus status;
	public Integer feedId;
}