package feedreader.web.config;

import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Guice module for data source objects and services
 * @author jared.pearson
 */
public class DataSourceModule extends AbstractModule {
	
	@Override
	protected void configure() {
	}
	
	@Provides
	@Singleton
	DataSource createDataSource(@Named("configuration") final Properties configuration) {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getProperty("dataSource.serverName"));
		dataSource.setPortNumber(Integer.valueOf(configuration.getProperty("dataSource.portNumber")));
		dataSource.setDatabaseName(configuration.getProperty("dataSource.databaseName"));
		dataSource.setUser(configuration.getProperty("dataSource.user"));
		dataSource.setPassword(configuration.getProperty("dataSource.password"));
		return dataSource;
	}
	
}