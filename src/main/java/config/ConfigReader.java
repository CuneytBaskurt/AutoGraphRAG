package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
	private static final Properties properties = new Properties();

	static {
		try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("Application.properties")) {
			if (input == null) {
				throw new RuntimeException("Application.properties file couldn't find!");
			}
			properties.load(input);
		} catch (IOException e) {
			throw new RuntimeException("An error occurred while loading the properties file: " + e.getMessage(), e);
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
