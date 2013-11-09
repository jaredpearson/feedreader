package common.messagequeue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Service in charge of sending messages
 * @author jared.pearson
 *
 */
public class MessageSender {
	private final Context context;
	private final ConnectionFactory connectionFactory;
	
	public MessageSender(final Context context, final ConnectionFactory connectionFactory) {
		this.context = context;
		this.connectionFactory = connectionFactory;
	}
	
	/**
	 * Sends the specified message to the specified address.
	 */
	public void send(final String address, final MessageProducer messageProducer) {
		try {
			Connection cnn = connectionFactory.createConnection();
			try {
				javax.jms.Session session = cnn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
				Message message = messageProducer.toMessage(new common.messagequeue.Session(session));
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