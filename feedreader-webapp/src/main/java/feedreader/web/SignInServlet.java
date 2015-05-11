package feedreader.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import common.persist.EntityManager;
import common.persist.EntityManagerFactory;
import feedreader.User;
import feedreader.persist.UserSessionEntityHandler;

public class SignInServlet extends HttpServlet {
	private static final long serialVersionUID = -6377418793578002487L;
	private EntityManagerFactory entityManagerFactory;
	private DataSource dataSource;
	private UserSessionEntityHandler userSessionEntityHandler;
	
	@Inject
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}
	
	@Inject
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Inject
	public void setUserSessionEntityHandler(UserSessionEntityHandler userSessionEntityHandler) {
		this.userSessionEntityHandler = userSessionEntityHandler;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			EntityManager entityManager = entityManagerFactory.get();
			
			//get the user information
			String email = request.getParameter("email");
			if(email == null || email.trim().length() == 0) {
				throw new ServletException("Email is required");
			}
			
			//get the user account associated to the specified email
			List<User> users = entityManager.executeNamedQuery(User.class, "getUserByEmail", email);
			if(users.isEmpty()) {
				throw new ServletException("Email address is unknown");
			}
			User user = users.get(0);
	
			//create a new session for the user
			final int sessionId = createSessionForUser(entityManager, user);
			
			//add the new cookie and send to the reader
			response.addCookie(new Cookie(AuthorizationFilter.SESSION_ID_COOKIE_NAME, String.valueOf(sessionId)));
			response.sendRedirect("/reader");
		} catch(SQLException exc) {
			throw new ServletException(exc);
		}
	}
	
	private int createSessionForUser(EntityManager entityManager, User user) throws SQLException {
		Connection cnn = dataSource.getConnection();
		try {
			return userSessionEntityHandler.insert(cnn, user.getId());
		} finally {
			cnn.close();
		}
	}
}
