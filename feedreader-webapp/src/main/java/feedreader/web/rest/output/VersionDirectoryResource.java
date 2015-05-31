package feedreader.web.rest.output;

import javax.annotation.Nonnull;

/**
 * Resource that represents a directory of versions. 
 * @author jared.pearson
 */
public class VersionDirectoryResource {
	public final ResourceLink[] versions;
	
	public VersionDirectoryResource(@Nonnull ResourceLink[] versions) {
		assert versions != null : "versions should not be null";
		this.versions = versions;
	}
}