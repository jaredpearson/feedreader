package feedreader.web.rest.output;

import javax.annotation.Nonnull;

/**
 * Resource that represents a version.
 * @author jared.pearson
 */
public class VersionResource {
	public final String name;
	public final ResourceLink[] actions;
	
	public VersionResource(@Nonnull String name, @Nonnull ResourceLink[] actions) {
		assert name != null : "name should not be null";
		assert actions != null : "actions should not be null";
		this.name = name;
		this.actions = actions;
	}
}