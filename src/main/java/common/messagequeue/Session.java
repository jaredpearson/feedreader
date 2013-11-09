package common.messagequeue;

import javax.jms.JMSException;

public class Session {
	private final javax.jms.Session session;
	
	public Session(javax.jms.Session session) {
		this.session = session;
	}
	
	public Message createMessage(String type) {
		try {
			Message mapMessageWrapper = Message.create(session);
			mapMessageWrapper.setType(type);
			return mapMessageWrapper;
		} catch(JMSException exc) {
			throw new RuntimeException(exc);
		}
	}
}
