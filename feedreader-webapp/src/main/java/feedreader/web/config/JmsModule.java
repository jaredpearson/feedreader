package feedreader.web.config;

import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import common.Provider;
import common.messagequeue.api.MessageHandler;
import common.messagequeue.api.MessageSender;
import common.messagequeue.jms.JmsMessageConsumer;
import common.messagequeue.jms.JmsMessageSender;
import feedreader.fetch.FeedLoader;
import feedreader.messagequeue.RetrieveFeedMessageBuilder;
import feedreader.messagequeue.RetrieveFeedMessageHandler;
import feedreader.persist.FeedEntityHandler;
import feedreader.persist.FeedItemEntityHandler;
import feedreader.persist.FeedRequestEntityHandler;
import feedreader.persist.FeedSubscriptionEntityHandler;

/**
 * Guice module for configuring JMS
 * @author jared.pearson
 */
public class JmsModule extends AbstractModule {
	
	@Override
	protected void configure() {
	}
	
	@Provides
	@Singleton
	@Named("jms")
	Context provideJmsInitalContext(@Named("configuration") final Properties configuration) {
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
	
	@Provides
	@Singleton
	ConnectionFactory createConnectionFactory(@Named("jms") final Context context) {
		try {
			ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("mq_feedreader");
			return connectionFactory;
		} catch(NamingException exc) {
			throw new RuntimeException(exc);
		}
	}

	@Provides
	@Singleton
	MessageSender createMessageSender(ConnectionFactory connectionFactory, @Named("jms") final Context context) {
		return new JmsMessageSender(context, connectionFactory);
	}
	
	@Provides
	@Singleton
	JmsMessageConsumer createMessageConsumer(final ConnectionFactory connectionFactory, 
			@Named("jms") final Context context, 
			final DataSource dataSource,
			final FeedEntityHandler feedEntityHandler,
			final FeedItemEntityHandler feedItemEntityHandler,
			final FeedRequestEntityHandler feedRequestEntityHandler,
			final FeedSubscriptionEntityHandler subscriptionEntityHandler) {
		JmsMessageConsumer messageConsumer = new JmsMessageConsumer(connectionFactory, lookupDestination(context));
		
		messageConsumer.registerHandler(RetrieveFeedMessageBuilder.class.getName(), new Provider<MessageHandler>() {
			@Override
			public MessageHandler get() {
				// have the handler process the received message
				Provider<FeedLoader> feedLoaderProvider = new Provider<FeedLoader>() {
					@Override
					public FeedLoader get() {
						return new FeedLoader();
					}
				};
				return new RetrieveFeedMessageHandler(feedLoaderProvider, dataSource, feedEntityHandler, feedItemEntityHandler, feedRequestEntityHandler, subscriptionEntityHandler);
			}
		});
		
		return messageConsumer;
	}
		
	private Destination lookupDestination(final Context context) {
		try {
			return (Destination)context.lookup("feedRequest");
		} catch (NamingException exc) {
			throw new RuntimeException(exc);
		}
	}
}