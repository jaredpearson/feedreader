package feedreader.web;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;

import common.web.rest.ResourceHandler;
import feedreader.web.rest.FeedResourceHandler;
import feedreader.web.rest.FeedSubscriptionResourceHandler;
import feedreader.web.rest.RestAuthorizationFilter;
import feedreader.web.rest.ServiceResourceHandler;
import feedreader.web.rest.StreamResourceHandler;

/**
 * Guice module for configuring web servlets and filters
 * @author jared.pearson
 */
class WebModule extends ServletModule {

	@Override
	protected void configureServlets() {
		super.configureServlets();
		
		configureRestServices();
	}

	private void configureRestServices() {
		filter("/reader").through(AuthorizationFilter.class);
		
		bind(HomeServlet.class).in(Scopes.SINGLETON);
		serve("/home").with(HomeServlet.class);

		bind(SignInServlet.class).in(Scopes.SINGLETON);
		serve("/signIn").with(SignInServlet.class);
		
		bind(CreateUserServlet.class).in(Scopes.SINGLETON);
		serve("/createUser").with(CreateUserServlet.class);
		
		bind(ReaderServlet.class).in(Scopes.SINGLETON);
		serve("/reader").with(ReaderServlet.class);
		
		//configure the rest servlet
		filter("/services/*", "/services").through(RestAuthorizationFilter.class);
		Multibinder<ResourceHandler> handlerBinder = Multibinder.newSetBinder(binder(), ResourceHandler.class);
		handlerBinder.addBinding().to(FeedSubscriptionResourceHandler.class);
		handlerBinder.addBinding().to(FeedResourceHandler.class);
		handlerBinder.addBinding().to(ServiceResourceHandler.class);
		handlerBinder.addBinding().to(StreamResourceHandler.class);
		serve("/services/*", "/services").with(common.web.rest.RestServlet.class);
	}

}