package de.ebf.utils;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Config {
	
	public static Configuration instance;
	
	static{
		try {
			AbstractConfiguration.setDefaultListDelimiter(';');
			instance = new PropertiesConfiguration("LocalSettings.properties");
		} catch (ConfigurationException e) {
			throw new RuntimeException("Unable to read required config file "+e);
		}
	}
	
}
