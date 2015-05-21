package feedreader.utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * Database utilities.
 * @author jared.pearson
 */
public class DbUtils {
	private DataSource dataSource;
	
	/**
	 * Gets the currently configured datasource
	 */
	public @Nonnull DataSource getDataSource() {
		if (this.dataSource == null) {
			final PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
			pgDataSource.setServerName("192.168.52.13");
			pgDataSource.setPortNumber(5432);
			pgDataSource.setDatabaseName("feedreader");
			pgDataSource.setUser("feedreader_app");
			pgDataSource.setPassword("zUSAC7HbtXcVMkk");
			this.dataSource = pgDataSource;
		}
		return this.dataSource;
	}
	
	/**
	 * Gets a connection from the datasource
	 */
	public @Nonnull Connection getConnection() throws SQLException {
		return this.getDataSource().getConnection();
	}
}
