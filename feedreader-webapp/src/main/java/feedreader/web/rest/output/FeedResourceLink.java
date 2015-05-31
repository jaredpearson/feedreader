package feedreader.web.rest.output;

/**
 * Represents a link to a feed
 * @author jared.pearson
 */
public class FeedResourceLink {
	public final int id;
	public final String title;
	public final String href;
	
	public FeedResourceLink(int id, String title, String href) {
		this.id = id;
		this.title = title;
		this.href = href;
	}
}