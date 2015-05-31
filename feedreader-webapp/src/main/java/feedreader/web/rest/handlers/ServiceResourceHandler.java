package feedreader.web.rest.handlers;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import feedreader.web.rest.output.ResourceHrefBuilder;
import feedreader.web.rest.output.ResourceLink;
import feedreader.web.rest.output.VersionDirectoryResource;
import feedreader.web.rest.output.VersionResource;

/**
 * Resource handler for the REST-ful service
 * @author jared.pearson
 */
@Singleton
public class ServiceResourceHandler implements ResourceHandler {
	private static Version[] SUPPORTED_VERSIONS = new Version[] {
		new Version("v1", new VersionAction[]{
			new VersionAction("stream", "/stream")
		})
	};
	
	@RequestHandler("^/?$")
	public VersionDirectoryResource showVersions(HttpServletRequest httpRequest) throws Exception {
		final ResourceLink[] versions = new ResourceLink[SUPPORTED_VERSIONS.length];
		for(int index = 0; index < SUPPORTED_VERSIONS.length; index++) {
			final Version version = SUPPORTED_VERSIONS[index];
			final String href = new ResourceHrefBuilder(httpRequest, version.name).buildHref("");
			versions[index] = new ResourceLink(version.name, href);
		}

		return new VersionDirectoryResource(versions);
	}
	
	@RequestHandler("^/(v[0-9]+)")
	public VersionResource showVersion(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathParameter(1) String versionName) throws Exception {
		
		//find the version resource corresponding to the name specified
		Version version = null;
		for(Version thisVersion : SUPPORTED_VERSIONS) {
			if(thisVersion.getName().equals(versionName)) {
				version = thisVersion;
				break;
			}
		}
		if(version == null) {
			httpResponse.sendError(404);
			return null;
		}
		
		final ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(httpRequest, version.getName());
		
		//convert to a resource
		final ResourceLink[] versionLinks = new ResourceLink[version.getActions().length];
		for(int index = 0; index < version.getActions().length; index++) {
			final VersionAction versionAction = version.getActions()[index];
			final String href = hrefBuilder.buildHref(versionAction.getPath());
			
			versionLinks[index] = new ResourceLink(versionAction.name, href);
		}

		return new VersionResource(version.getName(), versionLinks);
	}
	
	static class Version {
		private final String name;
		private final VersionAction[] actions;
		
		public Version(final String name, final VersionAction[] actions) {
			this.name = name;
			this.actions = actions;
		}
		
		public String getName() {
			return name;
		}
		
		public VersionAction[] getActions() {
			return actions;
		}
	}
	
	static class VersionAction {
		private final String name;
		private final String path;
		
		public VersionAction(String name, String path) {
			this.name = name;
			this.path = path;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPath() {
			return path;
		}
	}
}
