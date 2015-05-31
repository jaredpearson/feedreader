package feedreader.web.rest.output;

import java.util.Date;

/**
 * Resource for the Feed entity.
 * @author jared.pearson
 */
public class FeedResource {
	public int id;
	public String title;
	public Date created;
	public String url;
	public FeedItemResource[] items;
}