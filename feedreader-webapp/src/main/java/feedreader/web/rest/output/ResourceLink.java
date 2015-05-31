package feedreader.web.rest.output;

/**
 * Resource for a generic link to another resource 
 * @author jared.pearson
 */
public class ResourceLink {
	public final String name;
	public final String href;
	
	public ResourceLink(String name, String href) {
		this.name = name;
		this.href = href;
	}
	
}