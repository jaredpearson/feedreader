package common.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
	public T mapRow(ResultSet rst) throws SQLException;
}