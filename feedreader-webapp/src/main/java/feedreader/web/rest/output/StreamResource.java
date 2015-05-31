package feedreader.web.rest.output;

import javax.annotation.Nonnull;

/**
 * Resource that represents the Stream entity.
 * @author jared.pearson
 */
public class StreamResource {
	public final FeedItemResource[] items;
	
	public StreamResource(@Nonnull FeedItemResource[] items) {
		assert items != null : "items should not be null";
		this.items = items;
	}
}