package common.persist;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionHandler {
	public Object handle(Connection cnn) throws SQLException;
}