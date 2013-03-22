package de.ebf.utils;

import java.util.ResourceBundle;

public class Bundle {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("messages");

	public static String getString(String string) {
		return bundle.getString(string);
	}

}
