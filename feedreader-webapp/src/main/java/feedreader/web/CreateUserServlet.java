package feedreader.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.persist.EntityManagerFactory;
import feedreader.User;

public class CreateUserServlet extends HttpServlet {
	private static final long serialVersionUID = 4450852222264476357L;
	private EntityManagerFactory entityManagerFactory;
	
	@Inject
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String email = request.getParameter("email");
		if(email == null || email.trim().length() == 0) {
			throw new ServletException("Email is required");
		}
		
		User user = new User();
		user.setEmail(email);
		entityManagerFactory.get().persist(user);
		
		response.sendRedirect("/reader");
	}
}
