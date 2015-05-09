package common.messagequeue.api;

/**
 * Service in charge of sending messages
 * @author jared.pearson
 */
public interface MessageSender {

	/**
	 * Sends the message created by the specified builder to the specified address.
	 */
	public void send(final String address, final MessageBuilder messageBuilder);
	
}