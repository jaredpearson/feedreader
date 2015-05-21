package feedreader.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.IOUtils;

/**
 * Servlet that always responds with a sample RSS
 * @author jared.pearson
 */
@Singleton
public class RssSampleServlet extends HttpServlet {
	private static final long serialVersionUID = -1233427250801271631L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final File sampleFile = getSampleFile(request);
		if (sampleFile == null) {
			response.sendError(404);
			return;
		}
		
		response.setContentLength((int)sampleFile.length());
		
		final OutputStream outputStream = response.getOutputStream();
		try {
			final InputStream inputStream = new FileInputStream(sampleFile);
			try {
				
				IOUtils.write(outputStream, inputStream);
				
			} finally {
				inputStream.close();
			}
		} finally {
			outputStream.flush();
			outputStream.close();
		}
		
	}
	
	private File getSampleFile(HttpServletRequest request) throws IOException {
		try {
			final String pathInfo = request.getPathInfo();
			if (pathInfo.startsWith("/sample1")) {
				return new File(Thread.currentThread().getContextClassLoader().getResource("sample1.xml").toURI());
			} else if (pathInfo.startsWith("/sample2")) {
				return new File(Thread.currentThread().getContextClassLoader().getResource("sample2.xml").toURI());
			} else {
				return new File(Thread.currentThread().getContextClassLoader().getResource("rss2sample.xml").toURI());
			}
	
		} catch(URISyntaxException exc) {
			throw new RuntimeException(exc);
		}
	}
}
