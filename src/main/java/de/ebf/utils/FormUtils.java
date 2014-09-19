package de.ebf.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class FormUtils {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    private static final String IPADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    
    private static final String URL_PATTERN = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private final static Pattern emailPattern;
    private final static Pattern ipPattern;
    private final static Pattern urlPattern;


    static {
        emailPattern    = Pattern.compile(EMAIL_PATTERN);
        ipPattern       = Pattern.compile(IPADDRESS_PATTERN);
        urlPattern      = Pattern.compile(URL_PATTERN);
    }

    public static boolean isEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isIP(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        Matcher matcher = ipPattern.matcher(ip);
        return matcher.matches();
    }
    
    public static boolean isURL(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        Matcher matcher = urlPattern.matcher(url);
        return matcher.matches();
    }
}
