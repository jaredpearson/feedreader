package feedreader.web.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Servlet listener to configure Guice.
 * @author jared.pearson
 */
public class GuiceConfigListener extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(
			new DataSourceModule(),
			new JmsModule(),
			new FeedReaderModule(), 
			new JacksonModule(),
			new WebModule());
	}
}
