package feedreader.web;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.persist.EntityManager;
import common.persist.EntityManagerFactory;
import feedreader.User;
import feedreader.UserSession;

public class SignInServlet extends HttpServlet {
	private static final long serialVersionUID = -6377418793578002487L;
	private EntityManagerFactory entityManagerFactory;
	
	@Inject
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
		UserSession session = createSessionForUser(entityManager, user);
		
		//add the new cookie and send to the reader
		response.addCookie(new Cookie(AuthorizationFilter.SESSION_ID_COOKIE_NAME, String.valueOf(session.getId())));
		response.sendRedirect("/reader");
	}
	
	private UserSession createSessionForUser(EntityManager entityManager, User user) {
		UserSession userSession = new UserSession();
		userSession.setUser(user);
		entityManager.persist(userSession);
		return userSession;
	}
}
