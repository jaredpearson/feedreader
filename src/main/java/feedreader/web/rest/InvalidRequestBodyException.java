package feedreader.web.rest;

/**
 * Exception that is thrown when the body of a request is not formatted properly
 * or contains an invalid value.
 * @author jared.pearson
 */
public class InvalidRequestBodyException extends RuntimeException {
	private static final long serialVersionUID = 8686268421633385696L;

	public InvalidRequestBodyException() {
		super();
	}

	public InvalidRequestBodyException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRequestBodyException(String message) {
		super(message);
	}

	public InvalidRequestBodyException(Throwable cause) {
		super(cause);
	}
	
}
