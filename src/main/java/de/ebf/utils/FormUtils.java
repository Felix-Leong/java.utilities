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

    private final static Pattern emailPattern;
    private final static Pattern ipPattern;

    private static Matcher emailMatcher;
    private static Matcher ipMatcher;

    static {
        emailPattern = Pattern.compile(EMAIL_PATTERN);
        ipPattern = Pattern.compile(IPADDRESS_PATTERN);
    }

    public static boolean isEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        emailMatcher = emailPattern.matcher(email);
        return emailMatcher.matches();
    }

    public static boolean isIP(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        ipMatcher = ipPattern.matcher(ip);
        return ipMatcher.matches();
    }
}
