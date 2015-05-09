package common.messagequeue.api;

import javax.annotation.Nonnull;

/**
 * Represents a message. Instances of these can be created using {@link Session#createMessage(String)}
 * @author jared.pearson
 */
public interface Message {

	/**
	 * Gets the type of the message, which is used by the message consumer to 
	 * know who handles the message.
	 */
	public @Nonnull String getType();

	/**
	 * Sets the property to an integer value
	 */
	public void setInt(@Nonnull String name, Integer value);

	/**
	 * Gets the property value as an integer. If the property is not defined, then a null
	 * reference is returned.
	 */
	public Integer getInt(@Nonnull String name);

}