package common.web.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied to a method that processes requests
 * @author jared.pearson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestHandler {
	
	/**
	 * Regular expression that matches the Path Info of the request 
	 */
	String value();
	
	/**
	 * The request method that the handler method is applied to. The
	 * default is Method.GET.
	 */
	Method method() default Method.GET;
}