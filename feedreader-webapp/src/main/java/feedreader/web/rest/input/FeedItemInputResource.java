package feedreader.web.rest.input;

/**
 * Input value for updating a Feed Item
 * @author jared.pearson
 */
public class FeedItemInputResource {
	private Boolean read = null;
	private boolean isReadSet = false;
	
	public Boolean getRead() {
		return read;
	}
	
	public void setRead(Boolean read) {
		this.read = read;
		this.isReadSet = true;
	}
	
	/**
	 * Determines if the read value was set using the setter. This is useful for 
	 * determining if the value was specified in the request.
	 */
	public boolean isReadSet() {
		return isReadSet;
	}
}