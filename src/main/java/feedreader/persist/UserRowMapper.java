package feedreader.persist;

import java.sql.ResultSet;
import java.sql.SQLException;

import common.persist.RowMapper;
import feedreader.User;

/**
 * Maps a row in a {@link ResultSet} to a {@link User}
 * @author jared.pearson
 */
public class UserRowMapper implements RowMapper<User> {
	private String prefix = null;
	
	/**
	 * 
	 * @param prefix The prefix of the output name used
	 */
	public UserRowMapper(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public User mapRow(ResultSet rst) throws SQLException {
		User user = new User();
		user.setId(rst.getInt(prefix + "id"));
		user.setEmail(rst.getString(prefix + "email"));
		return user;
	}
}