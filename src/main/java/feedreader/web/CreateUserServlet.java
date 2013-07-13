package feedreader.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.persist.EntityManager;

import feedreader.User;

public class CreateUserServlet extends HttpServlet {
	private static final long serialVersionUID = 4450852222264476357L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		EntityManager em = (EntityManager)getServletContext().getAttribute("feedreader.entityManager");
		
		String email = request.getParameter("email");
		if(email == null || email.trim().length() == 0) {
			throw new ServletException("Email is required");
		}
		
		User user = new User();
		user.setEmail(email);
		em.persist(user);
		
		response.sendRedirect("/index.html");
	}
}
