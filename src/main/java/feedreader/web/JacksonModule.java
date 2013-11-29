package feedreader.web;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.AbstractModule;

/**
 * Guice module for configuring Jackson
 * @author jared.pearson
 */
class JacksonModule extends AbstractModule {
	@Override
	protected void configure() {

		//configure Jackson ObjectMapper
		bind(ObjectMapper.class).toInstance(new ObjectMapper());
		
	}
}