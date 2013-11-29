package feedreader.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Injector;

import common.messagequeue.MessageConsumer;

/**
 * Listener invoked when the application is first started.
 * @author jared.pearson
 */
public class StartupListener implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		
		final Injector injector = (Injector)servletContext.getAttribute(Injector.class.getName());
		if(injector == null) {
			throw new RuntimeException("Injector has not been initialized");
		}
		
		//start the message consumer
		final MessageConsumer messageConsumer = injector.getInstance(MessageConsumer.class);
		new Thread(messageConsumer).start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}
	
}
