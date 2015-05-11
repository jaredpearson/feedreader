package feedreader.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import feedreader.persist.UserEntityHandler;

@Singleton
public class CreateUserServlet extends HttpServlet {
	private static final long serialVersionUID = 4450852222264476357L;
	private DataSource dataSource;
	private UserEntityHandler userEntityHandler;
	
	@Inject
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Inject
	public void setUserEntityHandler(UserEntityHandler userEntityHandler) {
		this.userEntityHandler = userEntityHandler;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		final String email = request.getParameter("email");
		if(email == null || email.trim().length() == 0) {
			throw new ServletException("Email is required");
		}
		
		try {
			final Connection cnn = dataSource.getConnection();
			try {
				userEntityHandler.insert(cnn, email);
			} finally {
				cnn.close();
			}
		} catch(SQLException exc) {
			throw new ServletException(exc);
		}
		
		response.sendRedirect("/reader");
	}
}
