package common.messagequeue.api;

import javax.annotation.Nonnull;

/**
 * A session is a short lived context for sending messages. 
 * @author jared.pearson
 */
public interface Session {
	
	/**
	 * Creates a new message associated to this session.
	 */
	public @Nonnull Message createMessage(@Nonnull String type);
}
