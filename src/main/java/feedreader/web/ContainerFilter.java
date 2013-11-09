package feedreader.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.ioc.ComponentAdapter;
import common.ioc.ComponentAdapters;
import common.ioc.Container;
import common.messagequeue.MessageSender;
import common.persist.EntityManager;
import feedreader.FeedReader;
import feedreader.User;
import feedreader.UserSession;

/**
 * Instantiates a new IOC Container for each request
 * @author jared.pearson
 */
public class ContainerFilter implements Filter {
	public static final String REQUEST_ATTRIBUTE = "feedreader.ioc.Container";
	private ServletContext servletContext;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.servletContext = filterConfig.getServletContext();
	}
	
	@Override
	public void destroy() {
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		//get the current container
		Container parentContainer = (Container)servletContext.getAttribute(REQUEST_ATTRIBUTE);
		
		//create the container for the current request
		Container container = createContainer(parentContainer, servletContext, httpRequest, httpResponse);
		assert container != null : "Container should never be null";
		request.setAttribute(REQUEST_ATTRIBUTE, container);
		
		chain.doFilter(request, response);
		
		//reset the parent container
		request.setAttribute(REQUEST_ATTRIBUTE, parentContainer);
	}
	
	/**
	 * Creates the container for the given request
	 */
	private Container createContainer(Container parentContainer, ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		Container container = null; 
		if(parentContainer != null) {
			container = new Container(parentContainer);
		} else {
			container = new Container();
		}
		
		container.addComponent(request);
		container.addComponent(response);
		container.addComponent(context);
		container.addAdapter(new ComponentAdapter<UserSession>() {
			@Override
			public Class<UserSession> getComponentClass() {
				return UserSession.class;
			}
			@Override
			public UserSession getComponentInstance(Container container) {
				HttpServletRequest request = container.getComponent(HttpServletRequest.class);
				if(request == null) {
					return null;
				}
				return (UserSession) request.getAttribute("feedreader.UserSession");
			}
		});
		container.addAdapter(ComponentAdapters.asSingleton(new ComponentAdapter<FeedReader>() {
			@Override
			public Class<FeedReader> getComponentClass() {
				return FeedReader.class;
			}
			@Override
			public FeedReader getComponentInstance(Container container) {
				EntityManager entityManager = container.getComponent(EntityManager.class);
				UserSession userSession = container.getComponent(UserSession.class);
				MessageSender messageSender = container.getComponent(MessageSender.class);
				User user = userSession.getUser();
				
				FeedReader feedReader = new FeedReader(entityManager, user, messageSender);
				return feedReader;
			}
		}));
		return container;
	}
	
	/**
	 * Gets the container from the specified request. If the container is not on the request,
	 * then <code>null</code> is returned.
	 */
	public static Container getContainerFromRequest(ServletRequest request) {
		return (Container) ((HttpServletRequest)request).getAttribute(REQUEST_ATTRIBUTE);
	}
}
