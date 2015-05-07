package common.web.rest;

import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import common.web.rest.RequestHandler;
import common.web.rest.ResourceHandler;
import common.web.rest.RestServlet;

public class RestServletTest {
	
	@Test
	public void testHandlerExecution() throws Exception {
		ServletConfig config = mock(ServletConfig.class);
		
		final HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("GET");
		when(request.getPathInfo()).thenReturn("/test");
		
		final HttpServletResponse response = mock(HttpServletResponse.class);
		
		Set<ResourceHandler> handlers = new HashSet<ResourceHandler>();
		handlers.add(new TestRequestHandler());
		
		RestServlet servlet = new RestServlet(handlers);
		servlet.init(config);
		servlet.injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(HttpServletResponse.class).toInstance(response);
			}
		});
		servlet.service(request, response);
		
		//no error should be thrown
		verify(response, never()).sendError(anyInt());
	}
	
	public static class TestRequestHandler implements ResourceHandler {
		@RequestHandler("/test")
		public void handle(HttpServletResponse response) {
		}
	}
}
