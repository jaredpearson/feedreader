package feedreader;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * Represents a page of {@link FeedRequest} instances
 * @author jared.pearson
 */
public class FeedRequestPage {
	private List<FeedRequest> items;
	private int total;
	
	public FeedRequestPage(@Nonnull List<FeedRequest> items, int total) {
		Preconditions.checkArgument(items != null, "items should not be null");
		this.items = Collections.unmodifiableList(items);
		this.total = total;
	}
	
	/**
	 * Gets the feed requests that are held within this page.
	 */
	public @Nonnull List<FeedRequest> getItems() {
		return this.items;
	}
	
	/**
	 * Gets the total number of feed requests that are stored. This number will be larger
	 * than <code>getItems().size()</code> when there are items that were not loaded within
	 * this page of items.
	 */
	public int getTotal() {
		return total;
	}
}
