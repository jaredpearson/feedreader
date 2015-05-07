package common.messagequeue;

import java.io.IOException;

public interface MessageHandler {
	public void dequeue(Message message) throws IOException;
}
