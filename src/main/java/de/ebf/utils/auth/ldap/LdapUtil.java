/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import de.ebf.utils.Config;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Dominik
 */
public class LdapUtil {

    private static final Logger log = Logger.getLogger(LdapUtil.class);
    public static String ATTR_USER_PW = "userPassword";
    public static String ATTR_OBJECTCLASS = "objectclass";
    public static String ATTR_LAST_NAME = "sn";
    public static String ATTR_MAIL = "mail";
    public static String ATTR_ENTRYUUID = "entryUUID";
    public static String ATTR_FIRST_NAME = "givenName";
    public static String ATTR_CN = "cn";
    public static String ATTR_UID = "uid";
    public static String ATTR_TELEPHONE_NUMBER = "telephoneNumber";
    public static String ATTR_MEMBERS = "uniqueMember";
    public static String OBJECTCLASS_USER = "inetOrgPerson";
    public static String OBJECTCLASS_GROUP = "groupOfUniqueNames";
    public static String OBJECT_CLASS_OU = "organizationalUnit";
    public static String OBJECT_CLASS_ORGANIZATION = "domain";
    public static String ATTR_DN = "entryDN";
    public static String[] ATTR_ALL;

    private static final Logger Log = Logger.getLogger(LdapUtil.class);
    
    private static Map<String, LDAPConnectionPool> poolMap = new HashMap<>();

    static {
        PropertiesConfiguration config = Config.instance;
        if (config.getString("ldap.type").equals(LdapType.ActiveDirectory.name())) {
            OBJECTCLASS_USER = "user";
            OBJECTCLASS_GROUP = "group";
            ATTR_ENTRYUUID = "objectGUID";
            ATTR_DN = "distinguishedName";
            ATTR_MEMBERS = "member";
            ATTR_USER_PW = "unicodePwd";
        }
        ATTR_ALL = new String[]{ATTR_CN, ATTR_DN, ATTR_FIRST_NAME, ATTR_LAST_NAME, ATTR_UID, ATTR_MAIL, ATTR_TELEPHONE_NUMBER, ATTR_ENTRYUUID, ATTR_MEMBERS};
    }

    public static String getDN(String name, String context) {
        if (!name.contains("=")) {
            return "cn=" + name + "," + context;
        }
        return name;
    }

    public static LDAPConnection getConnection(String userName, String password, String context) throws LDAPException {
        String user = getDN(userName, context);
        LDAPConnection conn = null;
        if (poolMap.containsKey(user)) {
            LDAPConnectionPool pool = poolMap.get(user);
            conn = pool.getConnection();
            if (conn != null && !conn.isConnected()) {
                log.info("Closing all LDAP connections in connection pool for user [" + user + "] b/c of invalid LDAP connection");
                pool.close();
                poolMap.remove(user);
            }
        }
        if (conn == null) {
            if (LdapConfig.getType().equals(LdapType.ActiveDirectory)){
                try {
                    SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
                    SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
                    // Establish a secure connection using the socket factory.
                    conn = new LDAPConnection(sslSocketFactory);
                    conn.connect(LdapConfig.getServer(), LdapConfig.getPort());

                    conn.bind(user, password);
                } catch (GeneralSecurityException ex) {
                    log.fatal(ex);
                }  
            } else {
                conn = new LDAPConnection(LdapConfig.getServer(), LdapConfig.getPort(), user, password);
            }
            //LDAPConnectionOptions connOptions = new LDAPConnectionOptions();
            //connOptions.setUseSchema(true);
            //conn.setConnectionOptions(connOptions);
            LDAPConnectionPool pool = new LDAPConnectionPool(conn, 100);
            //remove all LDAP connections after 15 mins
            pool.setMaxConnectionAgeMillis(15 * 60 * 1000);
            //check LDAP connection healt every 1 min
            pool.setHealthCheckIntervalMillis(60 * 1000);
            //create new connections immediately if pool is exhausted
            pool.setCreateIfNecessary(true);
            pool.setMaxWaitTimeMillis(0);
            pool.setConnectionPoolName(user);
            poolMap.put(user, pool);
        }
        return conn;
    }

    public static void release(LDAPConnection conn) {
        release(conn, false);
    }

    public static void release(LDAPConnection conn, Boolean defunct) {
        if (conn != null) {
            String connectionPoolName = conn.getConnectionPoolName();
            if (!StringUtils.isEmpty(connectionPoolName)) {
                LDAPConnectionPool pool = poolMap.get(connectionPoolName);
                if (pool != null) {
                    if (defunct) {
                        pool.releaseDefunctConnection(conn);
                    } else {
                        pool.releaseConnection(conn);
                    }
                } else {
                    log.warn("Unable to release LDAP connection due to missing connection pool.");
                }
            } else {
                log.warn("Unable to release LDAP connection due to empty connection pool name.");
            }
        }
    }

    public static String getCN(String dn) {
        String[] dnParts = dn.split(",");
        return dnParts[0].substring("cn=".length());
    }

    public static void removeConnection(String user, String context) {
        String userDN = getDN(user, context);
        LDAPConnectionPool pool = poolMap.get(userDN);
        if (pool != null) {
            pool.close();
        }
        poolMap.remove(userDN);
    }

    public static boolean isValidName(String name) {
        return !StringUtils.isEmpty(name) && !name.matches(".*(\\s|,;|=).*");
    }

    public static boolean isValidGroupName(String name) {
        return !StringUtils.isEmpty(name) && !name.matches(".*(,;|=).*");
    }

    /*
     ActiveDirectory stores objectGUID in binary format. For whatever fucking reason
     see http://www.developerscrappad.com/1109/windows/active-directory/java-ldap-jndi-2-ways-of-decoding-and-using-the-objectguid-from-windows-active-directory/
     */
    static String bytesToUUID(byte[] objectGUID) {
        StringBuilder displayStr = new StringBuilder();

        displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF));

        return displayStr.toString();
    }

    //not needed anymore
    private static String UUIDStringToUUIDByteString(String uuid) {
        byte[] objectGUID = UUIDStringToByteArray(uuid);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < objectGUID.length; i++) {
            String transformed = prefixZeros((int) objectGUID[i] & 0xFF);
            result.append("\\");
            result.append(transformed);
        }
        return result.toString();
    }

    //d130ff50-7963-424c-af64-3ecaa26e7262
    static byte[] UUIDStringToByteArray(String uuid) {
        uuid = uuid.replace("-", "");
        int len = uuid.length();
        byte[] data = new byte[len / 2];
        
        int[] order = new int[len/2];
        order[0] = 3;
        order[1] = 2;
        order[2] = 1;
        order[3] = 0;
        order[4] = 5;
        order[5] = 4;
        order[6] = 7;
        order[7] = 6;
        order[8] = 8;
        order[9] = 9;
        order[10] = 10;
        order[11] = 11;
        order[12] = 12;
        order[13] = 13;
        order[14] = 14;
        order[15] = 15;
        
        for (int i = 0; i < len; i += 2) {
            data[order[i/2]] = (byte) ((Character.digit(uuid.charAt(i), 16) << 4)
                                 + Character.digit(uuid.charAt(i+1), 16));
        }
        return data;
    }

    private static String prefixZeros(int value) {
        if (value <= 0xF) {
            StringBuilder sb = new StringBuilder("0");
            sb.append(Integer.toHexString(value));
            return sb.toString();
        } else {
            return Integer.toHexString(value);
        }
    }
}
