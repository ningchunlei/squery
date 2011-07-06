package org.squery.custom;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Config {
	private static final String defaultFileName = "config";
	private static final Log logger = LogFactory.getLog(Config.class);
	private static HashMap<String, Properties> propMap = new HashMap<String, Properties>();

	public static String getProperty(String key) {
		return getProperty(key, defaultFileName);
	}

	public static Properties loadProperties(String fileName) {
		InputStream in = Config.class.getClassLoader().getResourceAsStream(fileName + ".properties");
		Properties prop = new Properties();
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return prop;
	}

	public static void reload() {
		reload(defaultFileName);
	}

	public synchronized static void reload(String fileName) {
		Properties prop = loadProperties(fileName);
		propMap.put(fileName, prop);
	}

	public static String getProperty(String key, String fileName) {
		Properties prop = propMap.get(fileName);
		if (prop == null) {
			synchronized (Config.class) {
				if (prop == null) {
					if (logger.isInfoEnabled()) {
						logger.info("load properties file:" + fileName);
					}
					prop = loadProperties(fileName);
				}
				propMap.put(fileName, prop);
			}
		}
		String str = prop.getProperty(key);
		return str == null ? "" : str;
	}

	public static void main(String[] args) {
		System.out.println(Config.getProperty("kkk", "a"));
		System.out.println(Config.getProperty("kkk", "b"));
	}
}
