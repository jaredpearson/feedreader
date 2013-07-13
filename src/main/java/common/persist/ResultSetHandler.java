package common.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler {
	public Object handle(ResultSet rst) throws SQLException;
}