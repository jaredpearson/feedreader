package feedreader.web.config;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import common.persist.EntityManager;
import common.persist.EntityManager.EntityHandler;
import common.persist.EntityManagerFactory;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.FeedRequest;
import feedreader.FeedSubscription;
import feedreader.User;
import feedreader.UserFeedItemContext;
import feedreader.UserSession;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedItemEntityHandler;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.persist.FeedSubscriptionEntityHandler;
import feedreader.persist.UserEntityHandler;
import feedreader.persist.UserFeedItemContextEntityHandler;
import feedreader.persist.UserSessionEntityHandler;

/**
 * Guice module for data source objects and services
 * @author jared.pearson
 */
public class DataSourceModule extends AbstractModule {
	
	@Override
	protected void configure() {
	}
	
	@Provides
	@Singleton
	EntityManagerFactory createEntityManagerFactory(final DataSource dataSource) {
		return new EntityManagerFactory() {
			private EntityManager entityManager;
			
			@Override
			public EntityManager get() {
				if(entityManager == null) {
					entityManager = createEntityManager(dataSource);
				}
				return entityManager;
			}
		};
	}
	
	@Provides
	@Singleton
	DataSource createDataSource(@Named("configuration") final Properties configuration) {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getProperty("dataSource.serverName"));
		dataSource.setPortNumber(Integer.valueOf(configuration.getProperty("dataSource.portNumber")));
		dataSource.setDatabaseName(configuration.getProperty("dataSource.databaseName"));
		dataSource.setUser(configuration.getProperty("dataSource.user"));
		dataSource.setPassword(configuration.getProperty("dataSource.password"));
		return dataSource;
	}
	
	private static EntityManager createEntityManager(final DataSource dataSource) {
		Map<Class<?>, EntityHandler> handlers = new Hashtable<Class<?>, EntityManager.EntityHandler>();
		handlers.put(Feed.class, new FeedEntityHandler());
		handlers.put(FeedItem.class, new FeedItemEntityHandler());
		handlers.put(FeedRequest.class, new FeedRequestEntityHandler());
		handlers.put(FeedSubscription.class, new FeedSubscriptionEntityHandler());
		handlers.put(User.class, new UserEntityHandler());
		handlers.put(UserFeedItemContext.class, new UserFeedItemContextEntityHandler());
		handlers.put(UserSession.class, new UserSessionEntityHandler());
		
		EntityManager entityManager = new EntityManager(dataSource, handlers);
		return entityManager;
	}
}