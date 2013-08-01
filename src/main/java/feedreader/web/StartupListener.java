package feedreader.web;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.postgresql.ds.PGSimpleDataSource;

import common.persist.EntityManager;
import common.persist.EntityManager.EntityHandler;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.User;
import feedreader.UserFeedItemContext;
import feedreader.UserSession;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedItemEntityHandler;
import feedreader.persist.UserEntityHandler;
import feedreader.persist.UserFeedItemContextEntityHandler;
import feedreader.persist.UserSessionEntityHandler;

public class StartupListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		
		String configProperties = servletContext.getInitParameter("configProperties");
		if(configProperties == null) {
			throw new RuntimeException("Unable to find configProperties context-param. Ensure that the context-param is set in the web.xml to the location of the configuration properties file.");
		}
		
		Properties configuration = new Properties();
		try {
			configuration.load(StartupListener.class.getResourceAsStream(configProperties));
		} catch(IOException exc) {
			throw new RuntimeException(exc);
		}
		
		//add the datasource to servlet context
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getProperty("dataSource.serverName"));
		dataSource.setPortNumber(Integer.valueOf(configuration.getProperty("dataSource.portNumber")));
		dataSource.setDatabaseName(configuration.getProperty("dataSource.databaseName"));
		dataSource.setUser(configuration.getProperty("dataSource.user"));
		dataSource.setPassword(configuration.getProperty("dataSource.password"));
		servletContext.setAttribute("feedreader.dataSource", dataSource);
		
		//add the entity manager
		Map<Class<?>, EntityHandler> handlers = new Hashtable<Class<?>, EntityManager.EntityHandler>();
		handlers.put(User.class, new UserEntityHandler());
		handlers.put(UserSession.class, new UserSessionEntityHandler());
		handlers.put(Feed.class, new FeedEntityHandler());
		handlers.put(FeedItem.class, new FeedItemEntityHandler());
		handlers.put(UserFeedItemContext.class, new UserFeedItemContextEntityHandler());
		EntityManager entityManager = new EntityManager(dataSource, handlers);
		servletContext.setAttribute("feedreader.entityManager", entityManager);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}
}
