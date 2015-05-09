package common.messagequeue.api;

import javax.annotation.Nonnull;

/**
 * Message producers can be instantiated outside the bounds of a session and then 
 * are able to create messages when a session is available.
 * @author jared.pearson
 */
public interface MessageBuilder {
	
	/**
	 * Creates a message to be sent 
	 * @param session The current session
	 * @return the message to be sent
	 */
	public Message build(@Nonnull Session session);
	
}