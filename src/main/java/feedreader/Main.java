package feedreader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main {
	
	public static void main(String[] args) throws Exception {
		startServer();
	}

	public static void startServer() throws Exception {
		String webappDir = "src/main/webapp";
		
		String port = System.getenv("PORT");
		if(port == null || port.isEmpty()) {
			port = "8080";
		}
		
		WebAppContext webappContext = new WebAppContext();
		webappContext.setContextPath("/");
		webappContext.setDescriptor(webappDir + "/WEB-INF/web.xml");
		webappContext.setResourceBase(webappDir);
		webappContext.setParentLoaderPriority(true);
		
		Server server = new Server(Integer.valueOf(port));
		server.setHandler(webappContext);
		server.start();
		server.join();
	}
	
}
