package common.messagequeue;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * The message to be sent
 * @author jared.pearson
 */
public class Message {
	private static final String PROP_TYPE = "__type";
	private final MapMessage mapMessage;
	
	private Message(final MapMessage mapMessage) {
		this.mapMessage = mapMessage;
	}
	
	/**
	 * Specifies the type of the message, which is used by the message consumer to 
	 * know who handles the message.
	 */
	public void setType(String type) {
		try {
			mapMessage.setStringProperty(PROP_TYPE, type);
		} catch (JMSException exc) {
			throw new RuntimeException(exc);
		}
	}

	/**
	 * Gets the type of the message, which is used by the message consumer to 
	 * know who handles the message.
	 */
	public String getType() {
		try {
			return mapMessage.getStringProperty(PROP_TYPE);
		} catch (JMSException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	public void setInt(String name, int value) {
		try {
			mapMessage.setInt(name, value);
		} catch (JMSException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	public int getInt(String name) {
		try {
			return mapMessage.getInt(name);
		} catch (JMSException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Converts this message into a JMS message
	 */
	/*package*/ javax.jms.Message toJmsMessage() throws JMSException {
		return mapMessage;
	}
	
	/**
	 * Creates a new message for the given session
	 */
	/*package*/ static Message create(final javax.jms.Session session) throws JMSException {
		javax.jms.MapMessage mapMessage = session.createMapMessage();
		return new Message(mapMessage);
	}
	
	/**
	 * Creates a new message from the given JMS Session. This is used by the consumer of a
	 * queue to create messages.
	 */
	/*package*/ static Message fromJmsMessage(final javax.jms.Message jmsMessage) throws JMSException {
		assert jmsMessage != null;
		return new Message((MapMessage)jmsMessage);
	}

}
