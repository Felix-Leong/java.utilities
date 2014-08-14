/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import de.ebf.utils.Bundle;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Dominik
 */
public class LdapUtil {

    private static final Logger log = Logger.getLogger(LdapUtil.class);
 
    private static final Map<String, LDAPConnectionPool> poolMap = new HashMap<>();

    /**
     * Validate the ldap-related parameters in request. If successful, these
     * validated values will be written in specified tenant. If any error, the
     * tenant keeps unchanged.
     *
     * @param request
     * @throws Exception
     */
    public static void validateLdapParameters(HttpServletRequest request) throws Exception {
        String ldapTypeString = request.getParameter("ldapType");
        String ldapServer = request.getParameter("ldapServer");
        String ldapPortString = request.getParameter("ldapPort");
        String ldapUser = request.getParameter("ldapUser");
        String ldapPass = request.getParameter("ldapPass");
        String ldapBaseDN = request.getParameter("ldapBaseDN");
        LdapType ldapType = LdapType.valueOf(ldapTypeString);
        Integer ldapPort = Integer.parseInt(ldapPortString);
        

        if (ldapType.equals(LdapType.ActiveDirectory) && !(ldapPort != 636 || ldapPort!= 3269)) {
            throw new Exception(Bundle.getString("ActiveDirectoryPortRequirements"));
        }

        if (StringUtils.isEmpty(ldapServer) || StringUtils.isEmpty(ldapUser) || StringUtils.isEmpty(ldapPass) || StringUtils.isEmpty(ldapBaseDN)) {
            throw new Exception(Bundle.getString("RequiredFieldsMustNotBeEmpty"));
        }
    }
    
    public static void verifyConnection(LdapConfig config) throws Exception{
        LDAPConnection conn = null;
        try {
            conn = getConnection(config);
            conn.connect(config.getServer(), config.getPort());
            conn.bind(config.getUsername(), config.getPassword());
            Filter userFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_USER);
            SearchResult searchResult = conn.search(config.getBaseDN(), SearchScope.SUB, userFilter, config.getSchema().ATTR_CN);
            if (!searchResult.getResultCode().equals(ResultCode.SUCCESS) || searchResult.getEntryCount()<=0){
                throw new LdapException("The specified base DN does not contain any users.");
            }
            Filter groupFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_GROUP);
            searchResult = conn.search(config.getBaseDN(), SearchScope.SUB, groupFilter, config.getSchema().ATTR_CN);
            if (!searchResult.getResultCode().equals(ResultCode.SUCCESS) || searchResult.getEntryCount()<=0){
                throw new LdapException("The specified base DN does not contain any groups.");
            }
        } finally {
            release(conn);
        }
    }
    
    protected static LDAPConnection getConnection(LdapConfig config) throws LDAPException{
        return getConnection(config.getUsername(), config.getPassword(), config);
    }

    protected static LDAPConnection getConnection(String userName, String password, LdapConfig config) throws LDAPException {
        String user = getConnectionPoolKey(userName, config);
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
            conn = getUnpooledConnection(userName, password, config);
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
    
    /*
     * use with care
    */
    protected static LDAPConnection getUnpooledConnection(String user, String password, LdapConfig config) throws LDAPException{
        LDAPConnection conn = null;
        if (config.getType().equals(LdapType.ActiveDirectory)){
            try {
                SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
                SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
                // Establish a secure connection using the socket factory.
                conn = new LDAPConnection(sslSocketFactory);
            } catch (GeneralSecurityException ex) {
                log.fatal(ex);
            }  
        } else {
            conn = new LDAPConnection();
        }
        if (conn!=null){
            LDAPConnectionOptions options = new LDAPConnectionOptions();
            options.setConnectTimeoutMillis(10*1000);
            options.setResponseTimeoutMillis(10*1000);
            conn.setConnectionOptions(options);
            try {
                conn.connect(config.getServer(), config.getPort());
            } catch (LDAPException e){
                log.error("Error while connecting to "+config.getServer()+":"+config.getPort(), e);
                //If a fallback LDAP server is configured, use it
                String server2 = config.getServer2();
                Integer port2 = config.getPort2();
                if (!StringUtils.isEmpty(server2) && port2!=null){
                    log.info("Falling back to "+server2+":"+port2);
                    conn.connect(server2, port2);
                } else {
                    throw e;
                }
            }
            if (conn.isConnected()){
                conn.bind(user, password);
            }
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

    public static void removeConnection(String user, LdapConfig config) {
        String userDN = getConnectionPoolKey(user, config);
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
    
    private static String getConnectionPoolKey(String name, LdapConfig config) {
        return name + "@" + config.getServer() +":"+ config.getPort();
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
    
    
    //objectSid
    public static String bytesToSid(byte[] SID) {
        // Add the 'S' prefix
        StringBuilder strSID = new StringBuilder("S-");

   // bytes[0] : in the array is the version (must be 1 but might 
        // change in the future)
        strSID.append(SID[0]).append('-');

        // bytes[2..7] : the Authority
        StringBuilder tmpBuff = new StringBuilder();
        for (int t = 2; t <= 7; t++) {
            String hexString = Integer.toHexString(SID[t] & 0xFF);
            tmpBuff.append(hexString);
        }
        strSID.append(Long.parseLong(tmpBuff.toString(), 16));

        // bytes[1] : the sub authorities count
        int count = SID[1];

   // bytes[8..end] : the sub authorities (these are Integers - notice
        // the endian)
        for (int i = 0; i < count; i++) {
            int currSubAuthOffset = i * 4;
            tmpBuff.setLength(0);
            tmpBuff.append(String.format("%02X%02X%02X%02X",
                    (SID[11 + currSubAuthOffset] & 0xFF),
                    (SID[10 + currSubAuthOffset] & 0xFF),
                    (SID[9 + currSubAuthOffset] & 0xFF),
                    (SID[8 + currSubAuthOffset] & 0xFF)));

            strSID.append('-').append(Long.parseLong(tmpBuff.toString(), 16));
        }

        // That's it - we have the SID
        return strSID.toString();
    }
}
