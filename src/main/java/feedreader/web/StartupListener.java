package feedreader.web;

import java.util.Hashtable;
import java.util.Map;

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
		
		//add the datasource to servlet context
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName("127.0.0.1");
		dataSource.setPortNumber(5432);
		dataSource.setDatabaseName("feedreader");
		dataSource.setUser("jared.pearson");
		dataSource.setPassword("");
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
