package common.messagequeue.api;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Handles a message after it has been taken out of the queue.
 * @author jared.pearson
 */
public interface MessageHandler {
	/**
	 * Handles a message after it has been taken out of the queue.
	 */
	public void dequeue(@Nonnull Message message) throws IOException;
}
