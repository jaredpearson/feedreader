package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import com.google.common.base.Preconditions;

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
	
	/**
	 * Writes all of the bytes of the input stream to the output stream.
	 * @param outputStream the stream to write to
	 * @param inputStream the stream to read from
	 * @return the number of bytes written
	 */
	public static long write(OutputStream outputStream, InputStream inputStream) throws IOException {
		Preconditions.checkArgument(inputStream != null, "inputStream should not be null");
		Preconditions.checkArgument(outputStream != null, "outputStream should not be null");
		final ReadableByteChannel readByteChannel = Channels.newChannel(inputStream);
		try {
			final WritableByteChannel writeByteChannel = Channels.newChannel(outputStream);
			try {
				final ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 8);
				
				long size = 0;
				while (readByteChannel.read(byteBuffer) != -1) {
					byteBuffer.flip();
					size += writeByteChannel.write(byteBuffer);
					byteBuffer.clear();
				}
				
				return size;
			} finally {
				writeByteChannel.close();
			}
		} finally {
			readByteChannel.close();
		}
	}
}