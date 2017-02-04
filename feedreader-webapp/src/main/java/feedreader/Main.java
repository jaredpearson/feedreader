package feedreader;

import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Main class for starting the server.
 * @author jared.pearson
 */
public class Main {

    public static void main(String[] args) throws Exception {
        new Main().startServer();
    }

    private void startServer() throws Exception {
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
        webappContext.addServlet(jspServletHolder(), "*.jsp");
        webappContext.addBean(new JspStarter(webappContext));

        Server server = new Server(Integer.valueOf(port));
        server.setHandler(webappContext);
        server.start();
        server.join();
    }

    /**
     * Servlet for handling JSP requests
     */
    private ServletHolder jspServletHolder() {
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.7");
        holderJsp.setInitParameter("compilerSourceVM", "1.7");
        holderJsp.setInitParameter("keepgenerated", "true");
        return holderJsp;
    }

    /**
     * Configures JSP initialization on Jetty startup
     */
    private static class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller {
        private final JettyJasperInitializer sci;
        private final ServletContextHandler context;
        
        public JspStarter (ServletContextHandler context) {
            this.sci = new JettyJasperInitializer();
            this.context = context;
            this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
        }

        @Override
        protected void doStart() throws Exception {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            try {
                sci.onStartup(null, context.getServletContext());
                super.doStart();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }
}
