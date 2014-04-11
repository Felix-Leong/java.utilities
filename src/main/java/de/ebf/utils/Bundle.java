package de.ebf.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bundle {

    private static final ResourceBundle baseMessagesBundle  = ResourceBundle.getBundle("basemessages");
    private static final ResourceBundle messagesBundle      = ResourceBundle.getBundle("messages");
    
    public static String getString(String string) {
        try {
            return baseMessagesBundle.getString(string);
        } catch (MissingResourceException ex){
            return messagesBundle.getString(string);
        }
    }

}
