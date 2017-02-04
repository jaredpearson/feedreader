package feedreader.web;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReaderServlet extends HttpServlet {
    private static final long serialVersionUID = -217367977340304415L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Cookie cookie = Arrays.stream(request.getCookies())
                .filter((c) -> c.getName().equals(AuthorizationFilter.SESSION_ID_COOKIE_NAME))
                .findFirst()
                .get();
        request.setAttribute("sid", cookie.getValue());
        request.getRequestDispatcher("/WEB-INF/views/reader.jsp").forward(request, response);
    }
}
