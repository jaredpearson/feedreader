package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class IOUtils {
	
	private IOUtils() {
	}

	/**
	 * Reads all of the characters in the resource at the given path to a String.
	 */
	public static String readResource(String resource) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
			return IOUtils.readFile(inputStream);
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}
	}
	
	/**
	 * Reads all of the characters in the input stream to a String.
	 */
	public static String readFile(InputStream inputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			
			String line = null;
			while((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
			
		} finally {
			if(reader != null) {
				reader.close();
			}
		}
		return stringBuilder.toString();
	}
	
}