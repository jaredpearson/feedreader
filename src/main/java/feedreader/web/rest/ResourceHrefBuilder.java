package feedreader.web.rest;

import javax.servlet.http.HttpServletRequest;

class ResourceHrefBuilder {
	private final String protocol;
	private final String serverName;
	private final int serverPort;
	private final String servletPath;
	private final String version; 
	
	public ResourceHrefBuilder(HttpServletRequest request, String version) {
		assert request != null;
		this.protocol = (request.getProtocol().startsWith("HTTPS/")) ? "https" : "http";
		this.serverName = request.getServerName();
		this.serverPort = request.getServerPort();
		this.servletPath = request.getServletPath();
		this.version = version;
	}
	
	public String buildHref(String path) {
		boolean showPort = (protocol.equalsIgnoreCase("http") && serverPort != 80) || (protocol.equalsIgnoreCase("https") && serverPort != 443);
		
		return protocol + "://" + serverName + 
				((showPort) ? ":" + serverPort : "") + 
				servletPath + 
				(servletPath.endsWith("/") ? "" : "/") + 
				version + 
				(path == null ? "" : path);
	}
}