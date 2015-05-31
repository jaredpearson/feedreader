package feedreader.web.config;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;

import common.web.rest.ResourceHandler;
import feedreader.web.AuthorizationFilter;
import feedreader.web.CreateUserServlet;
import feedreader.web.HomeServlet;
import feedreader.web.ReaderServlet;
import feedreader.web.RssSampleServlet;
import feedreader.web.SignInServlet;
import feedreader.web.rest.RestAuthorizationFilter;
import feedreader.web.rest.handlers.FeedItemResourceHandler;
import feedreader.web.rest.handlers.FeedRequestResourceHandler;
import feedreader.web.rest.handlers.FeedResourceHandler;
import feedreader.web.rest.handlers.FeedSubscriptionResourceHandler;
import feedreader.web.rest.handlers.ServiceResourceHandler;
import feedreader.web.rest.handlers.StreamResourceHandler;

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
		
		serve("/rssSamples/*").with(RssSampleServlet.class);
		
		//configure the rest servlet
		filter("/services/*", "/services").through(RestAuthorizationFilter.class);
		Multibinder<ResourceHandler> handlerBinder = Multibinder.newSetBinder(binder(), ResourceHandler.class);
		handlerBinder.addBinding().to(FeedItemResourceHandler.class);
		handlerBinder.addBinding().to(FeedSubscriptionResourceHandler.class);
		handlerBinder.addBinding().to(FeedRequestResourceHandler.class);
		handlerBinder.addBinding().to(FeedResourceHandler.class);
		handlerBinder.addBinding().to(ServiceResourceHandler.class);
		handlerBinder.addBinding().to(StreamResourceHandler.class);
		serve("/services/*", "/services").with(common.web.rest.RestServlet.class);
	}

}