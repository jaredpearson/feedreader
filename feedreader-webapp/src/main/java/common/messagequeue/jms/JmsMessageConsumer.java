package common.messagequeue.jms;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import common.Provider;
import common.messagequeue.api.Message;
import common.messagequeue.api.MessageHandler;

/**
 * Service that consumes messages from an address. This service blocks
 * until a message is recieved and then routes to the configured handler.
 * @author jared.pearson
 */
public class JmsMessageConsumer implements Runnable {
	private final ConnectionFactory connectionFactory;
	private final Map<String, Provider<MessageHandler>> messageHandlers; 
	private final Destination destination;
	
	@Inject
	public JmsMessageConsumer(final ConnectionFactory connectionFactory, final Destination destination) {
		this.connectionFactory = connectionFactory;
		this.destination = destination;
		this.messageHandlers = new Hashtable<String, Provider<MessageHandler>>();
	}
	
	public void registerHandler(final String type, final Provider<MessageHandler> messageHandler) {
		this.messageHandlers.put(type, messageHandler);
	}
	
	@Override
	public void run() {
		try {
			final Connection connection = connectionFactory.createConnection();
			try {
				connection.start();
				
				final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				
				// receive the message
				javax.jms.MessageConsumer messageConsumer = session.createConsumer(destination);
				javax.jms.Message jmsMessage;
				while((jmsMessage = messageConsumer.receive()) != null) {
					
					//convert the JMS session into our own wrapper
					Message message = JmsMessage.fromJmsMessage(jmsMessage);
					
					//delegate the message to the handler
					MessageHandler handler = getMessageHandler(message);
					handler.dequeue(message);
					
				}
			} finally {
				if(connection != null) {
					connection.close();
				}
			}
		} catch(IOException exc) {
			throw new RuntimeException(exc);
		} catch(JMSException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private MessageHandler getMessageHandler(Message message) {
		if(!messageHandlers.containsKey(message.getType())) {
			throw new IllegalArgumentException(String.format("No message handler registered for type %s", message.getType()));
		}
		return messageHandlers.get(message.getType()).get();
	}
}