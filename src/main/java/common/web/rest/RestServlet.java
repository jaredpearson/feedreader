package common.web.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.ioc.Container;
import common.ioc.web.ContainerFilter;

/**
 * Servlet for handling REST Api requests.
 * @author jared.pearson
 */
public class RestServlet extends GenericServlet {
	private static final long serialVersionUID = 7150818916790895212L;
	private List<RequestHandlerMapping> requestMappings = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//parse the classes parameter
		List<Class<?>> requestHandlerClasses;
		try {
			requestHandlerClasses = parseClassCsv(config.getInitParameter("classes"));
		} catch (ClassNotFoundException exc) {
			throw new ServletException(exc);
		}
		
		//parse the RequestHandler annotations from the specified classes
		requestMappings = new ArrayList<RequestHandlerMapping>();
		for(Class<?> requestHandlerClass : requestHandlerClasses) {
			Object handler = null;
			
			for(java.lang.reflect.Method method : requestHandlerClass.getMethods()) {
				if(!method.isAnnotationPresent(RequestHandler.class)) {
					continue;
				}
				RequestHandler requestHandlerAnnotation = method.getAnnotation(RequestHandler.class);
				
				String pathRegex = requestHandlerAnnotation.value();
				Pattern pattern = Pattern.compile(pathRegex);
				
				//create the handler or get one that was already created
				if(handler == null) {
					try {
						handler = requestHandlerClass.newInstance();
					} catch (InstantiationException e) {
						throw new ServletException(e);
					} catch (IllegalAccessException e) {
						throw new ServletException(e);
					}
				}
				
				requestMappings.add(new RequestHandlerMapping(pattern, handler, method, requestHandlerAnnotation.method()));
			}
		}
	}

	@Override
	public void service(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		try {
			RequestHandlerMapping mapping = getMapping(httpRequest);
			mapping.execute(httpRequest);
		} catch(Exception exc) {
			exc.printStackTrace(System.err);
			httpResponse.sendError(500);
		}
	}
	
	/**
	 * Gets the mapping from the request. If the no mapping is found, then
	 * an {@link IllegalStateException} is thrown.
	 */
	private RequestHandlerMapping getMapping(HttpServletRequest request) {
		for(RequestHandlerMapping mapping : requestMappings) {
			if(mapping.handles(request)) {
				return mapping;
			}
		}
		
		throw new IllegalStateException("No mapping specified for request");
	}
	
	/**
	 * Parses a list of class names from the given comma-separated string
	 */
	private List<Class<?>> parseClassCsv(String classesValue) throws ClassNotFoundException {

		String[] classNames = null;
		if(classesValue == null || classesValue.trim().length() == 0) {
			classNames = new String[0];
		} else {
			classNames = classesValue.split(",");
		}
		
		//convert the classNames to classes
		List<Class<?>> requestHandlerClasses = new ArrayList<Class<?>>(classNames.length);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for(String className : classNames) {
			if(className == null || className.trim().isEmpty()) {
				continue;
			}
			
			Class<?> clazz = classLoader.loadClass(className.trim());
			requestHandlerClasses.add(clazz);
		}
		
		return requestHandlerClasses;
	}
	
	private static class RequestHandlerMapping {
		private final Pattern pattern;
		private final Object handler;
		private final Method method;
		private final java.lang.reflect.Method handlerMethod;
		
		public RequestHandlerMapping(Pattern pattern, Object handler, java.lang.reflect.Method handlerMethod, Method method) {
			this.pattern = pattern;
			this.handler = handler;
			this.handlerMethod = handlerMethod;
			this.method = method;
		}
		
		/**
		 * Determines if the request is mapped to this RequestHandler
		 */
		public boolean handles(HttpServletRequest request) {
			if(!request.getMethod().equals(method.toString())) {
				return false;
			}
			
			return getMatcher(request).matches();
		}
		
		/**
		 * Executes the handler against the specified request
		 */
		public void execute(final HttpServletRequest request)
			throws ServletException, IOException {
			
			//initialize the value retrievers
			PathParameterValueRetriever pathParameterValueRetriever = PathParameterValueRetriever.createFromRequest(request, this);
			ContainerValueRetriever containerValueRetriever = ContainerValueRetriever.createFromRequest(request);
			
			//we need to determine the value to be specified in the parameter.
			Annotation[][] parameterAnnotations = handlerMethod.getParameterAnnotations();
			Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
			Object[] values = new Object[parameterTypes.length];
			for(int index = 0; index < parameterTypes.length; index++) {
				Class<?> parameterType = parameterTypes[index];
				Annotation[] annotations = parameterAnnotations[index];
				
				//let's check to see if the path parameter annotation has been specified
				if(pathParameterValueRetriever.handles(index, parameterType, annotations)) {
					values[index] = pathParameterValueRetriever.getValue(index, parameterType, annotations);
					continue;
				}
				
				//from the parameter types specified on the method, look up the 
				//objects corresponding to the types from the IOC container
				values[index] = containerValueRetriever.getValue(index, parameterType, annotations);
			}
			
			//invoke the handler method with parameters requested
			try {
				handlerMethod.invoke(handler, values);
			} catch (IllegalArgumentException e) {
				throw new ServletException(e);
			} catch (IllegalAccessException e) {
				throw new ServletException(e);
			} catch (InvocationTargetException e) {
				throw new ServletException(e);
			}
		}
		
		private Matcher getMatcher(HttpServletRequest request) {
			String pathInfo = request.getPathInfo();
			return pattern.matcher(pathInfo);
		}
	}
	
	private static interface ValueRetriever {
		public boolean handles(int index, Class<?> parameterType, Annotation[] parameterAnnotations);
		public Object getValue(int index, Class<?> parameterType, Annotation[] parameterAnnotations);
	}
	
	/**
	 * Retrieves the values from the Container.
	 * @author jared.pearson
	 */
	private static class ContainerValueRetriever implements ValueRetriever {
		private final Container container;
		
		public ContainerValueRetriever(final Container container) {
			this.container = container;
		}
		
		@Override
		public boolean handles(int index, Class<?> parameterType, Annotation[] parameterAnnotations) {
			return true;
		}
		
		@Override
		public Object getValue(int index, Class<?> parameterType, Annotation[] parameterAnnotations) {
			return container.getComponent(parameterType);
		}
		
		public static ContainerValueRetriever createFromRequest(final HttpServletRequest request) {
			Container container = ContainerFilter.getContainerFromRequest(request);
			return new ContainerValueRetriever(container);
		}
	}
	
	/**
	 * Retrieves the values from the PathInfo using the {@link PathParameter} annotations.
	 * @author jared.pearson
	 */
	private static class PathParameterValueRetriever implements ValueRetriever {
		private final Matcher pathInfoMatcher;
		
		public PathParameterValueRetriever(final Matcher pathInfoMatcher) {
			this.pathInfoMatcher = pathInfoMatcher;
		}
		
		@Override
		public boolean handles(int index, Class<?> parameterType, Annotation[] parameterAnnotations) {
			return getPathParameterAnnotation(parameterAnnotations) != null;
		}
		
		@Override
		public Object getValue(int index, Class<?> parameterType, Annotation[] parameterAnnotations) {
			//let's check to see if the path parameter annotation has been specified
			PathParameter pathParameter = getPathParameterAnnotation(parameterAnnotations);
			if(pathParameter == null) {
				return null;
			}
			
			if(!parameterType.isAssignableFrom(String.class)) {
				throw new IllegalArgumentException("PathParameter must be of type String");
			}
			
			int groupIndex = pathParameter.value();
			
			if(groupIndex > pathInfoMatcher.groupCount()) {
				throw new IllegalArgumentException("Group index specified in PathParameter is out of bounds: " + groupIndex);
			}
			return pathInfoMatcher.group(groupIndex);
		}
		
		public static PathParameterValueRetriever createFromRequest(final HttpServletRequest request, final RequestHandlerMapping mapping) {
			Matcher pathInfoMatcher = mapping.getMatcher(request);
			if(!pathInfoMatcher.matches()) {
				throw new IllegalStateException("Expected the path to always match this handler's pattern");
			}
			return new PathParameterValueRetriever(pathInfoMatcher);
		}
		
		private PathParameter getPathParameterAnnotation(Annotation[] annotations) {
			PathParameter pathParameter = null;
			for(Annotation annotation : annotations) {
				if(annotation instanceof PathParameter) {
					pathParameter = (PathParameter)annotation;
					break;
				}
			}
			return pathParameter;
		}
	}
}
