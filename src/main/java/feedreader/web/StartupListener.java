package feedreader.web;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import common.ioc.ComponentAdapter;
import common.ioc.ComponentAdapters;
import common.ioc.Container;
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
		
		//get the config properties context-param
		String configProperties = servletContext.getInitParameter("configProperties");
		if(configProperties == null) {
			throw new RuntimeException("Unable to find configProperties context-param. Ensure that the context-param is set in the web.xml to the location of the configuration properties file.");
		}
		
		//load the properties file specified by the context-param
		final Properties configuration = new Properties();
		try {
			configuration.load(StartupListener.class.getResourceAsStream(configProperties));
		} catch(IOException exc) {
			throw new RuntimeException(exc);
		}
		
		//create the application container
		Container container = new Container();
		container.addAdapter(ComponentAdapters.asSingleton(new ComponentAdapter<javax.sql.DataSource>() {
			@Override
			public Class<javax.sql.DataSource> getComponentClass() {
				return javax.sql.DataSource.class;
			}
			
			@Override
			public DataSource getComponentInstance(Container container) {
				PGSimpleDataSource dataSource = new PGSimpleDataSource();
				dataSource.setServerName(configuration.getProperty("dataSource.serverName"));
				dataSource.setPortNumber(Integer.valueOf(configuration.getProperty("dataSource.portNumber")));
				dataSource.setDatabaseName(configuration.getProperty("dataSource.databaseName"));
				dataSource.setUser(configuration.getProperty("dataSource.user"));
				dataSource.setPassword(configuration.getProperty("dataSource.password"));
				return dataSource;
			}
		}));
		container.addAdapter(ComponentAdapters.asSingleton(new ComponentAdapter<EntityManager>() {
			@Override
			public Class<EntityManager> getComponentClass() {
				return EntityManager.class;
			}
			
			@Override
			public EntityManager getComponentInstance(Container container) {
				Map<Class<?>, EntityHandler> handlers = new Hashtable<Class<?>, EntityManager.EntityHandler>();
				handlers.put(User.class, new UserEntityHandler());
				handlers.put(UserSession.class, new UserSessionEntityHandler());
				handlers.put(Feed.class, new FeedEntityHandler());
				handlers.put(FeedItem.class, new FeedItemEntityHandler());
				handlers.put(UserFeedItemContext.class, new UserFeedItemContextEntityHandler());
				
				DataSource dataSource = container.getComponent(DataSource.class);
				EntityManager entityManager = new EntityManager(dataSource, handlers);
				return entityManager;
			}
		}));
		
		//set the container in the servlet context
		servletContext.setAttribute("feedreader.ioc.Container", container);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}
}
