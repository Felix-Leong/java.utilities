/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import de.ebf.utils.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Dominik
 */
public class LdapUtil {

   private static final Logger log = Logger.getLogger(LdapUtil.class);
   public static String ATTR_USER_PW                = "userPassword";
   public static String ATTR_OBJECTCLASS            = "objectclass";
   public static String ATTR_LAST_NAME              = "sn";
   public static String ATTR_MAIL                   = "mail";
   public static String ATTR_ENTRYUUID              = "entryUUID";
   public static String ATTR_FIRST_NAME             = "givenName";
   public static String ATTR_CN                     = "cn";
   public static String ATTR_UID                    = "uid";
   public static String ATTR_TELEPHONE_NUMBER       = "telephoneNumber";
   public static String ATTR_MEMBERS                = "uniqueMember";
   public static String OBJECTCLASS_USER            = "inetOrgPerson";
   public static String OBJECTCLASS_GROUP           = "groupOfUniqueNames";
   public static String OBJECT_CLASS_OU             = "organizationalUnit";
   public static String OBJECT_CLASS_ORGANIZATION   = "domain";
   public static String ATTR_DN                     = "entryDN";
   public static String[] ATTR_ALL;
   
   private static Map<String, LDAPConnectionPool> poolMap = new HashMap<>();
   
   
   static {
       PropertiesConfiguration config = Config.instance;
       switch (config.getString("ldap.type")){
           case "ActiveDirectory":
                OBJECTCLASS_USER    = "user";
                OBJECTCLASS_GROUP   = "group";
                ATTR_ENTRYUUID      = "objectGUID";
                ATTR_DN             = "distinguishedName";
               break;
           default:
               break;
               
       }
       ATTR_ALL = new String[]{ATTR_CN, ATTR_FIRST_NAME, ATTR_LAST_NAME, ATTR_UID, ATTR_MAIL, ATTR_TELEPHONE_NUMBER, ATTR_ENTRYUUID, ATTR_MEMBERS};
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
         conn = new LDAPConnection(LdapConfig.getServer(), LdapConfig.getPort(), user, password);
         LDAPConnectionOptions connOptions = new LDAPConnectionOptions();
         connOptions.setUseSchema(true);
         conn.setConnectionOptions(connOptions);
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

    static UUID bytesToUUID(byte[] bytes) {
        if (bytes != null && bytes.length == 16) {
            long msb = bytes[3] & 0xFF;
            msb = msb << 8 | (bytes[2] & 0xFF);
            msb = msb << 8 | (bytes[1] & 0xFF);
            msb = msb << 8 | (bytes[0] & 0xFF);

            msb = msb << 8 | (bytes[5] & 0xFF);
            msb = msb << 8 | (bytes[4] & 0xFF);

            msb = msb << 8 | (bytes[7] & 0xFF);
            msb = msb << 8 | (bytes[6] & 0xFF);

            long lsb = bytes[8] & 0xFF;
            lsb = lsb << 8 | (bytes[9] & 0xFF);
            lsb = lsb << 8 | (bytes[10] & 0xFF);
            lsb = lsb << 8 | (bytes[11] & 0xFF);
            lsb = lsb << 8 | (bytes[12] & 0xFF);
            lsb = lsb << 8 | (bytes[13] & 0xFF);
            lsb = lsb << 8 | (bytes[14] & 0xFF);
            lsb = lsb << 8 | (bytes[15] & 0xFF);

            return new UUID(msb, lsb);
        }
        return null;
    }
}
