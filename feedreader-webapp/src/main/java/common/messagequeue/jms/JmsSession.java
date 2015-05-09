package common.messagequeue.jms;

import javax.jms.JMSException;

import com.google.common.base.Preconditions;
import common.messagequeue.api.Session;

/**
 * JMS implementation of a {@link Session}
 * @author jared.pearson
 */
public class JmsSession implements common.messagequeue.api.Session {
	private final javax.jms.Session session;
	
	public JmsSession(javax.jms.Session session) {
		Preconditions.checkArgument(session != null, "session should not be null");
		this.session = session;
	}
	
	@Override
	public JmsMessage createMessage(String type) {
		try {
			JmsMessage mapMessageWrapper = JmsMessage.create(session);
			mapMessageWrapper.setType(type);
			return mapMessageWrapper;
		} catch(JMSException exc) {
			throw new RuntimeException(exc);
		}
	}
}
