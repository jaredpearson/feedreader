package feedreader.web.rest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import feedreader.web.AuthorizationFilter;

public class RestAuthorizationFilter extends AuthorizationFilter {
	
	/**
	 * Gets the session ID from the request header.
	 */
	@Override
	protected int getSessionId(HttpServletRequest httpRequest) throws ServletException {
		String authorizationHeader = httpRequest.getHeader("Authorization");
		if(authorizationHeader == null) {
			throw new ServletException("Authorization header must be specified");
		}
		if(!authorizationHeader.startsWith("SID ")) {
			throw new ServletException("Unsupported Authorization type specified");
		}
		String sidValue = authorizationHeader.substring("SID ".length());
		
		int sessionId = -1;
		try {
			sessionId = Integer.valueOf(sidValue);
		} catch(NumberFormatException exc) {
			return -1;
		}
		return sessionId;
	}
	
}
