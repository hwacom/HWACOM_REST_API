package com.hwacom.rest.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.springframework.boot.autoconfigure.integration.IntegrationProperties.RSocket.Client;

public final class MoeConfig {

	private static final Properties properties = new Properties();

	static {
		try {
			//ClassLoader loader = Thread.currentThread().getContextClassLoader();
			properties.load(new InputStreamReader(Client.class.getClassLoader().getResourceAsStream("config/moe.properties"), "UTF-8"));
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
    public static String getSetting(String key) {
        return properties.getProperty(key);
    }
}

