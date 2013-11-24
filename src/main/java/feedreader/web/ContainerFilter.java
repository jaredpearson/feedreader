package feedreader.web;

import javax.servlet.ServletContext;
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
 * Specialization of the IOC container filter. This is needed so that we can provide custom components in 
 * the container during the request.
 * @author jared.pearson
 */
public class ContainerFilter extends common.ioc.web.ContainerFilter {
	
	@Override
	protected Container createRequestContainer(Container parentContainer, ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		Container container = super.createRequestContainer(parentContainer, context, request, response);

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
	
}
