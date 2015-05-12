package feedreader.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import feedreader.User;
import feedreader.persist.UserEntityHandler;
import feedreader.persist.UserSessionEntityHandler;

public class SignInServlet extends HttpServlet {
	private static final long serialVersionUID = -6377418793578002487L;
	private DataSource dataSource;
	private UserEntityHandler userEntityHandler;
	private UserSessionEntityHandler userSessionEntityHandler;
	
	@Inject
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Inject
	public void setUserEntityHandler(UserEntityHandler userEntityHandler) {
		this.userEntityHandler = userEntityHandler;
	}
	
	@Inject
	public void setUserSessionEntityHandler(UserSessionEntityHandler userSessionEntityHandler) {
		this.userSessionEntityHandler = userSessionEntityHandler;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				
				//get the user information
				final String email = request.getParameter("email");
				if(email == null || email.trim().length() == 0) {
					throw new ServletException("Email is required");
				}
				
				//get the user account associated to the specified email
				final User user = userEntityHandler.findUserByEmail(cnn, email);
				if(user == null) {
					throw new ServletException("Email address is unknown");
				}
		
				//create a new session for the user
				final int sessionId = userSessionEntityHandler.insert(cnn, user.getId());
				
				//add the new cookie and send to the reader
				response.addCookie(new Cookie(AuthorizationFilter.SESSION_ID_COOKIE_NAME, String.valueOf(sessionId)));
				response.sendRedirect("/reader");
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw new ServletException(exc);
		}
	}
}
