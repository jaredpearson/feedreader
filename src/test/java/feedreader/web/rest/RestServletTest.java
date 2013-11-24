package feedreader.web.rest;

import static org.mockito.Mockito.*;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import common.ioc.Container;
import common.web.rest.RequestHandler;
import common.web.rest.RestServlet;
import feedreader.web.ContainerFilter;

public class RestServletTest {
	
	@Test
	public void testHandlerExecution() throws Exception {
		ServletConfig config = mock(ServletConfig.class);
		when(config.getInitParameter(eq("classes"))).thenReturn(TestRequestHandler.class.getName());
		
		Container container = mock(Container.class);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("GET");
		when(request.getPathInfo()).thenReturn("/test");
		when(request.getAttribute(eq(ContainerFilter.REQUEST_ATTRIBUTE))).thenReturn(container);
		
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		RestServlet servlet = new RestServlet();
		servlet.init(config);
		servlet.service(request, response);
		
		//no error should be thrown
		verify(response, never()).sendError(anyInt());
	}
	
	public static class TestRequestHandler {
		@RequestHandler("/test")
		public void handle(HttpServletResponse response) {
		}
	}
}
