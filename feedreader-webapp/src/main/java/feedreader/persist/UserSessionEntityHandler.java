package feedreader.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import common.persist.DbUtils;
import common.persist.EntityManager;
import common.persist.EntityManager.EntityHandler;
import feedreader.User;
import feedreader.UserSession;

public class UserSessionEntityHandler implements EntityHandler {
	
	@Override
	public List<Object> executeNamedQuery(EntityManager.QueryContext queryContext, String query, Object... parameters)
			throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object get(EntityManager.QueryContext queryContext, Object id) throws SQLException {
		UserSession session = null;
		int sessionId = (Integer)id;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("select s.id sessionId, s.created, u.id userId, u.email from UserSessions s inner join Users u on s.userId = u.id where s.id = ? limit 1");
				stmt.setInt(1, sessionId);
				
				ResultSet rst = null;
				try {
					rst = stmt.executeQuery();
					while(rst.next()) {
						session = new UserSession();
						session.setId(rst.getInt("sessionId"));
						session.setCreated(rst.getDate("created"));
						
						User user = new User();
						user.setId(rst.getInt("userId"));
						user.setEmail(rst.getString("email"));
						session.setUser(user);
					}
					
				} finally {
					DbUtils.close(rst);
				}
			} finally {
				DbUtils.close(stmt);
			}
			
		} finally {
			queryContext.releaseConnection(cnn);
		}
		
		return session;
	}
	
	@Override
	public void persist(EntityManager.QueryContext queryContext, Object entity) throws SQLException {
		UserSession userSession = (UserSession)entity;
		Connection cnn = null;
		try {
			cnn = queryContext.getConnection();
			
			PreparedStatement stmt = null;
			try {
				stmt = cnn.prepareStatement("insert into UserSessions (userId) values (?) returning id, created");
				stmt.setInt(1, userSession.getUser().getId());
				
				if(stmt.execute()) {
					ResultSet rst = null;
					try {
						rst = stmt.getResultSet();
						
						if(rst.next()) {
							userSession.setId(rst.getInt(1));
							userSession.setCreated(rst.getDate(2));
						}
					} finally {
						DbUtils.close(rst);
					}
				}
			} finally {
				DbUtils.close(stmt);
			}
		} finally {
			queryContext.releaseConnection(cnn);
		}
	}

}
