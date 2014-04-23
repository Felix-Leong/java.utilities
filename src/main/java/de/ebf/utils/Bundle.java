package de.ebf.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;


public class Bundle {

    private static ResourceBundle baseMessagesBundle; 
    private static ResourceBundle messagesBundle;
    
    private static final Logger log = Logger.getLogger(Bundle.class);
    
    static {
        messagesBundle = ResourceBundle.getBundle("messages");
        try {
            baseMessagesBundle = ResourceBundle.getBundle("basemessages");
        } catch (MissingResourceException e){
            log.info("Missing basemessages.properties. Using messages.properties only.");
        }
    }
   
    public static String getString(String key) {
        try {
            return messagesBundle.getString(key);
        } catch (MissingResourceException ex){
            if (baseMessagesBundle!=null){
                try {
                    return baseMessagesBundle.getString(key);
                } catch (MissingResourceException e){
                    //ignore
                }
            }
        }
        log.warn("Missing translation for key ["+key+"]");
        return key;
    }

}
