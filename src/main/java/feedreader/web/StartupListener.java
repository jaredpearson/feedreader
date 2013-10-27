package feedreader.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

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

/**
 * Listener invoked when the application is first started.
 * @author jared.pearson
 */
public class StartupListener implements ServletContextListener {
	private static final Logger logger = Logger.getLogger(StartupListener.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		
		//get the config properties context-param
		String configProperties = servletContext.getInitParameter("configProperties");
		if(configProperties == null) {
			throw new RuntimeException("Unable to find configProperties context-param. Ensure that the context-param is set in the web.xml to the location of the configuration properties file.");
		}
		
		//load the properties files specified by the context-param
		final Properties configuration = loadProperties(configProperties);
		
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
	
	/**
	 * Loads properties files from the specified comma-separated, resource paths.
	 */
	private Properties loadProperties(final String value) {
		assert value != null;
		String[] filePaths = value.split(",");
		Properties rootProperties = null;
		for(String filePath : filePaths) {
			//skip any empty paths
			if(filePath.trim().length() == 0) {
				continue;
			}
			
			Properties configuration = new Properties(rootProperties);
			try {
				InputStream stream = StartupListener.class.getResourceAsStream(filePath.trim());
				if(stream == null) {
					logger.fine("Unable to find configuration properties: " + filePath);
					continue;
				}
				configuration.load(stream);
			} catch(IOException exc) {
				throw new RuntimeException(exc);
			}
			rootProperties = configuration;
		}
		return rootProperties;
	}
}
