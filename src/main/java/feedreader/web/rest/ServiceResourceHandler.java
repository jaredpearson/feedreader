package feedreader.web.rest;

import java.io.Writer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;

public class ServiceResourceHandler implements ResourceHandler {
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
		
		for(int index = 0; index < versionApiNames.length; index++) {
			String versionApiName = versionApiNames[index];
			VersionResourceLink version1Link = new VersionResourceLink();
			version1Link.name = versionApiName;
			version1Link.href = new ResourceHrefBuilder(httpRequest, versionApiName).buildHref("");
			versions.versions[index] = version1Link;
		}
		
		httpResponse.setContentType("application/json");
		Writer out = httpResponse.getWriter();
		try {
			objectMapper.writeValue(out, versions);
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
	
}
