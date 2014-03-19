package de.ebf.utils;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Config {

    public static PropertiesConfiguration instance;

    static {
        load();
    }

    public static void load(String basePath) {
        try {
            AbstractConfiguration.setDefaultListDelimiter(';');
            instance = new PropertiesConfiguration();
            instance.setEncoding("UTF-8");
            if(basePath!=null){
                instance.setBasePath(basePath);
            }
            instance.load("LocalSettings.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("Unable to read required config file " + e);
        }
    }
    
    public static void load() {
        load(null);
    }
    
}
