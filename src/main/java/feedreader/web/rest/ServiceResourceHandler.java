package feedreader.web.rest;

import java.io.Writer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import common.web.rest.PathParameter;
import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;

public class ServiceResourceHandler implements ResourceHandler {
	private static Version[] SUPPORTED_VERSIONS = new Version[] {
		new Version("v1", new VersionAction[]{
			new VersionAction("stream", "/stream")
		})
	};
	private final ObjectMapper objectMapper;
	
	@Inject
	public ServiceResourceHandler(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@RequestHandler("^/?$")
	public void showVersions(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
		String[] versionApiNames = new String[]{"v1"};
		
		VersionsResourceLink versions = new VersionsResourceLink();
		versions.versions = new VersionResourceLink[versionApiNames.length];
		
		for(int index = 0; index < SUPPORTED_VERSIONS.length; index++) {
			Version version = SUPPORTED_VERSIONS[index];
			VersionResourceLink versionLink = new VersionResourceLink();
			versionLink.name = version.name;
			versionLink.href = new ResourceHrefBuilder(httpRequest, version.name).buildHref("");
			versions.versions[index] = versionLink;
		}
		
		httpResponse.setContentType("application/json");
		Writer out = httpResponse.getWriter();
		try {
			objectMapper.writeValue(out, versions);
		} finally {
			out.close();
		}
	}
	
	@RequestHandler("^/(v[0-9]+)")
	public void showVersionRoot(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathParameter(1) String versionName) throws Exception {
		
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
			return;
		}
		
		ResourceHrefBuilder hrefBuilder = new ResourceHrefBuilder(httpRequest, version.getName());
		
		//convert to a resource
		VersionResource versionResource = new VersionResource();
		versionResource.name = version.getName();
		versionResource.actions = new VersionActionResourceLink[version.getActions().length];
		for(int index = 0; index < version.getActions().length; index++) {
			VersionAction versionAction = version.getActions()[index];
			VersionActionResourceLink versionActionLink = new VersionActionResourceLink();
			versionActionLink.name = versionAction.name;
			versionActionLink.href = hrefBuilder.buildHref(versionAction.getPath());
			versionResource.actions[index] = versionActionLink;
		}
		
		//output as JSON
		httpResponse.setContentType("application/json");
		Writer out = httpResponse.getWriter();
		try {
			objectMapper.writeValue(out, versionResource);
		} finally {
			out.close();
		}
	}
	
	static class VersionsResourceLink {
		public VersionResourceLink[] versions;
	}
	
	static class VersionResourceLink {
		public String name;
		public String href;
	}
	
	static class VersionResource {
		public String name;
		public VersionActionResourceLink[] actions;
	}
	
	static class VersionActionResourceLink {
		public String name;
		public String href;
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
