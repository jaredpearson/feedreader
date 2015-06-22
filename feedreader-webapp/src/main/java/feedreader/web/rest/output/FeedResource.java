package feedreader.web.rest.output;

import java.util.Date;

/**
 * Resource for the Feed entity.
 * @author jared.pearson
 */
public class FeedResource {
	public final int id;
	public final String title;
	public final Date created;
	public final String url;
	public final FeedItemResource[] items;

	public FeedResource(
			int id,
			String title,
			Date created,
			String url,
			FeedItemResource[] items) {
		assert items != null : "items should not be null";
		this.id = id;
		this.title = title;
		this.created = created;
		this.url = url;
		this.items = items;
	}
}