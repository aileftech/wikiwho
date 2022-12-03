package tech.ailef.wikiwho.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class GzipReader implements Closeable {
	private BufferedReader reader;
	
	private InputStream gzipStream;
	
	public GzipReader(String filename) throws IOException {
		InputStream fileStream = new FileInputStream(filename);
		gzipStream = new GZIPInputStream(fileStream);
		reader = new BufferedReader(new InputStreamReader(gzipStream));
	}
	
	public String nextLine() throws IOException {
		return reader.readLine();
	}
	
	public void close() throws IOException {
		reader.close();
	}
	
	public InputStream getInputStream() {
		return gzipStream;
	}
}
