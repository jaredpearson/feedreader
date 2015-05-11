package feedreader.web.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.servlet.ServletScopes;

import common.messagequeue.api.MessageSender;
import common.persist.EntityManagerFactory;
import feedreader.FeedReader;
import feedreader.User;
import feedreader.UserSession;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.persist.UserFeedItemContextEntityHandler;
import feedreader.web.StartupListener;

/**
 * Guice module for all entities and services in the FeedReader
 * @author jared.pearson
 */
class FeedReaderModule extends AbstractModule {
	private static final Logger logger = Logger.getLogger(FeedReaderModule.class.getName());
	
	@Override
	public void configure() {
		bind(FeedReader.class).toProvider(FeedReaderModule.FeedReaderProvider.class).in(ServletScopes.REQUEST);
		bind(UserSession.class).toProvider(FeedReaderModule.UserSessionProvider.class).in(ServletScopes.REQUEST);
	}

	@Provides
	@Singleton
	@Named("configuration")
	Properties loadConfiguration(ServletContext servletContext) {

		//get the config properties context-param
		String configProperties = servletContext.getInitParameter("configProperties");
		if(configProperties == null) {
			throw new RuntimeException("Unable to find configProperties context-param. Ensure that the context-param is set in the web.xml to the location of the configuration properties file.");
		}
		
		//load the properties files specified by the context-param
		return loadProperties(configProperties);
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

	private static final class UserSessionProvider implements Provider<UserSession> {
		
		@Inject
		Provider<HttpServletRequest> request;
		
		@Override
		public UserSession get() {
			final UserSession userSession = (UserSession)request.get().getAttribute(UserSession.class.getName());
			if(userSession == null) {
				throw new IllegalStateException("No user session found. Is the AuthorizationFilter configured for the request?");
			}
			return userSession;
		}
	}
	
	private static final class FeedReaderProvider implements Provider<FeedReader> {
		@Inject
		EntityManagerFactory entityManagerFactory;
		
		@Inject
		MessageSender messageSender;
		
		@Inject
		UserSession userSession;
		
		@Inject
		DataSource dataSource;
		
		@Inject
		FeedEntityHandler feedEntityHandler;
		
		@Inject
		UserFeedItemContextEntityHandler userFeedItemContextEntityHandler;
		
		@Inject
		FeedRequestEntityHandler feedRequestEntityHandler;

		@Override
		public FeedReader get() {
			final User user = userSession.getUser();
			return new FeedReader(dataSource, entityManagerFactory, user, messageSender, feedEntityHandler, userFeedItemContextEntityHandler, feedRequestEntityHandler);
		}
	}
}