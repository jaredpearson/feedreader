package feedreader.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import common.Provider;
import common.ioc.ComponentAdapter;
import common.ioc.ComponentAdapters;
import common.ioc.Container;
import common.messagequeue.MessageConsumer;
import common.messagequeue.MessageHandler;
import common.messagequeue.MessageSender;
import common.persist.EntityManager;
import common.persist.EntityManager.EntityHandler;
import feedreader.Feed;
import feedreader.FeedItem;
import feedreader.FeedRequest;
import feedreader.FeedSubscription;
import feedreader.User;
import feedreader.UserFeedItemContext;
import feedreader.UserSession;
import feedreader.fetch.FeedLoader;
import feedreader.messagequeue.RetrieveFeedMessage;
import feedreader.messagequeue.RetrieveFeedMessageHandler;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedItemEntityHandler;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.persist.FeedSubscriptionEntityHandler;
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
		//set the container in the servlet context
		Container container = createContainer(configuration);
		servletContext.setAttribute("feedreader.ioc.Container", container);
		
		//start the message consumer
		(new Thread(container.getComponent(MessageConsumer.class))).start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
	}
	
	private Container createContainer(final Properties configuration) {
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
				handlers.put(Feed.class, new FeedEntityHandler());
				handlers.put(FeedItem.class, new FeedItemEntityHandler());
				handlers.put(FeedRequest.class, new FeedRequestEntityHandler());
				handlers.put(FeedSubscription.class, new FeedSubscriptionEntityHandler());
				handlers.put(User.class, new UserEntityHandler());
				handlers.put(UserFeedItemContext.class, new UserFeedItemContextEntityHandler());
				handlers.put(UserSession.class, new UserSessionEntityHandler());
				
				DataSource dataSource = container.getComponent(DataSource.class);
				EntityManager entityManager = new EntityManager(dataSource, handlers);
				return entityManager;
			}
		}));
		
		(new JmsModule()).initialize(container, configuration);
		
		return container;
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
	
	private static class JmsModule {
		public void initialize(final Container container, final Properties configuration) {
			//create the JNDI information for JMS
			final Context context = createInitalContext(configuration);
			
			container.addAdapter(new ComponentAdapter<ConnectionFactory>() {
				@Override
				public Class<ConnectionFactory> getComponentClass() {
					return ConnectionFactory.class;
				}
				
				@Override
				public ConnectionFactory getComponentInstance(Container container) {
					try {
						ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("mq_feedreader");
						return connectionFactory;
					} catch(NamingException exc) {
						throw new RuntimeException(exc);
					}
				}
			});
			container.addAdapter(ComponentAdapters.asSingleton(new ComponentAdapter<MessageConsumer>() {
				@Override
				public Class<MessageConsumer> getComponentClass() {
					return MessageConsumer.class;
				}
				@Override
				public MessageConsumer getComponentInstance(final Container container) {
					ConnectionFactory connectionFactory = container.getComponent(ConnectionFactory.class);
					MessageConsumer messageConsumer = new MessageConsumer(connectionFactory, lookupDestination());
					
					messageConsumer.registerHandler(RetrieveFeedMessage.class.getName(), new Provider<MessageHandler>() {
						@Override
						public MessageHandler get() {
							// have the handler process the received message
							EntityManager entityManager = container.getComponent(EntityManager.class);
							Provider<FeedLoader> feedLoaderProvider = new Provider<FeedLoader>() {
								@Override
								public FeedLoader get() {
									return new FeedLoader();
								}
							};
							return new RetrieveFeedMessageHandler(entityManager, feedLoaderProvider);
						}
					});
					
					return messageConsumer;
				}
				
				private Destination lookupDestination() {
					try {
						return (Destination)context.lookup("feedRequest");
					} catch (NamingException exc) {
						throw new RuntimeException(exc);
					}
				}
			}));
			container.addAdapter(new ComponentAdapter<MessageSender>() {
				@Override
				public Class<MessageSender> getComponentClass() {
					return MessageSender.class;
				}
				@Override
				public MessageSender getComponentInstance(Container container) {
					ConnectionFactory connectionFactory = container.getComponent(ConnectionFactory.class);
					return new MessageSender(context, connectionFactory);
				}
			});
		}
		
		private Context createInitalContext(final Properties configuration) {
			Context context;
			try {
				Properties props = new Properties();
				props.setProperty("java.naming.factory.initial", "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
				props.setProperty("connectionfactory.mq_feedreader", configuration.getProperty("qpid.connectionfactory.mq_feedreader"));
				props.setProperty("destination.feedRequest", "feed.request");
				context = new InitialContext(props);
			} catch (NamingException exc) {
				throw new RuntimeException(exc);
			}
			return context;
		}
	}
}
