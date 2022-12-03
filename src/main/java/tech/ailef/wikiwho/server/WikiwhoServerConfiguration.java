package tech.ailef.wikiwho.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class WikiwhoServerConfiguration {
	private static WikiwhoServerConfiguration instance = null;

	private Properties props;
	
	public static synchronized WikiwhoServerConfiguration getInstance() {
		if (instance == null)
			instance = new WikiwhoServerConfiguration();
		return instance;
	}

	private WikiwhoServerConfiguration() {
		props = new Properties();
		try {
			props.load(new FileInputStream(new File("config/server.conf")));
			System.out.println("Wikiwho starting with configuration: " + props);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String get(String property) {
		String val = props.getProperty(property);
		if (val == null) throw new IllegalArgumentException("Property " + property + " is null in Wikiwho server configuration");
		return val;
	}
	
	public int getInt(String property) {
		return Integer.parseInt(get(property));
	}
	
	public boolean getBoolean(String property) {
		return Boolean.valueOf(get(property));
	}
	

	public String getOrDefault(String property, String defaultValue) {
		return props.getProperty(property, defaultValue);
	}

	public Double getDouble(String property) {
		return Double.valueOf(get(property));
	}
}
