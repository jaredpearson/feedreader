package common.messagequeue.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

import com.google.common.base.Preconditions;

import common.messagequeue.api.MessageBuilder;
import common.messagequeue.api.MessageSender;

/**
 * Service in charge of sending messages
 * @author jared.pearson
 */
public class JmsMessageSender implements MessageSender {
	private final Context context;
	private final ConnectionFactory connectionFactory;
	
	public JmsMessageSender(final Context context, final ConnectionFactory connectionFactory) {
		this.context = context;
		this.connectionFactory = connectionFactory;
	}
	
	/**
	 * Sends the message created by the specified builder to the specified address.
	 */
	@Override
	public void send(final String address, final MessageBuilder messageBuilder) {
		Preconditions.checkArgument(address != null && !address.isEmpty(), "address should not be null");
		Preconditions.checkArgument(messageBuilder != null, "messageBuilder should not be null");
		
		try {
			Connection cnn = connectionFactory.createConnection();
			try {
				javax.jms.Session session = cnn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
				JmsMessage message = (JmsMessage)messageBuilder.build(new common.messagequeue.jms.JmsSession(session));
				javax.jms.Message jmsMessage = message.toJmsMessage();
				
				javax.jms.MessageProducer producer = session.createProducer(lookupDestination(address));
				producer.send(jmsMessage);
			} finally {
				cnn.close();
			}
		} catch(JMSException exc) {
			throw new RuntimeException(exc);
		}
	}

	private Destination lookupDestination(final String address) {
		try {
			return (Destination)context.lookup(address);
		} catch (NamingException exc) {
			throw new RuntimeException(exc);
		}
	}
}