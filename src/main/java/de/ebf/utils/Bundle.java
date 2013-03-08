package de.ebf.utils;

import java.util.ResourceBundle;

public class Bundle {
	
	public static final String KEY_SPECIFY_ALL_REQUIRED_FIELDS 		= "specifyAllRequiredFields";
	public static final String KEY_CONSTRAINT_VIOLATION_MESSAGE 	= "constraintViolationMessage";
	public static final String KEY_UNABLE_TO_DELETE_OBJECT 			= "unableToDeleteObject";
	public static final String KEY_EMAIL_SENT 						= "emailSent";
	public static final String KEY_EMAIL_NOT_SENT 					= "emailNotSent";
	public static final String KEY_UNKNOWN_ERROR 					= "unknownError";
	public static final String KEY_MISSING_REQUIRED_FIELD 			= "missingRequiredField";
	public static final String KEY_NO_WRITE_ACCESS 					= "noWriteAccess";
	public static final String KEY_MASTER_PASSWORD_HINT 			= "masterPasswordHint";
	public static final String KEY_MISSING_REQUIRED_JRE_FILES		= "MISSING_REQUIRED_JRE_FILES";
	public static final String KEY_UPDATE_SUCCESS 					= "updateSucces";
	public static final String KEY_UNKNOWN_USER 					= "unknownUser";
	public static final String KEY_INVALID_FIELD 					= "invalidField";
	public static final String KEY_INVALID_MOBILE_IRON_CREDENTIALS 	= "invalidMobileIronCredentials";
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("messages");

	public static String getString(String string) {
		return bundle.getString(string);
	}

}
