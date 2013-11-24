package feedreader.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.ioc.Container;
import common.ioc.web.ContainerFilter;
import common.persist.EntityManager;
import feedreader.UserSession;

/**
 * Filter for checking if a web request is authorized.
 * @author jared.pearson
 */
public class AuthorizationFilter implements Filter {
	public static final String SESSION_ID_COOKIE_NAME = "sid";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		
		Container container = ContainerFilter.getContainerFromRequest(httpRequest);
		EntityManager entityManager = container.getComponent(EntityManager.class);
		
		//attempt to get the session ID from cookie
		int sessionId = getSessionId(httpRequest);
		if(sessionId == -1) {
			httpResponse.sendError(401);
			return;
		}
		
		//load the session to see if it is still active
		UserSession userSession = entityManager.get(UserSession.class, sessionId);
		if(userSession.isExpired()) {
			httpResponse.sendError(401);
			return;
		}
		
		httpRequest.setAttribute("feedreader.UserSession", userSession);
		
		chain.doFilter(httpRequest, httpResponse);
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	/**
	 * Gets the session ID from the request. Return -1 if there is no session associated
	 * to the request.
	 */
	protected int getSessionId(HttpServletRequest httpRequest) throws ServletException {

		//try to get the session cookie and if not found then throw an error
		Cookie cookie = getCookieWithName(httpRequest, SESSION_ID_COOKIE_NAME);
		if(cookie == null || cookie.getValue() == null) {
			return -1;
		}
		
		//attempt to get the session ID from cookie
		int sessionId = -1;
		try {
			sessionId = Integer.valueOf(cookie.getValue());
		} catch(NumberFormatException exc) {
			return -1;
		}
		
		return sessionId;
	}
	
	private Cookie getCookieWithName(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
			return null;
		}
		
		Cookie foundCookie = null;
		for(Cookie cookie : cookies) {
			if(name.equals(cookie.getName())) {
				foundCookie = cookie;
				break;
			}
		}
		return foundCookie;
	}
}
