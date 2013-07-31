/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Dominik
 */
public class LdapUtil {

   private static final Logger log = Logger.getLogger(LdapUtil.class);
   public static final String ATTR_USER_PW = "userPassword";
   public static final String ATTR_OBJECTCLASS = "objectclass";
   public static final String ATTR_OBJECTCLASS_USER = "inetOrgPerson";
   public static final String ATTR_OBJECTCLASS_GROUP = "groupOfUniqueNames";
   public static final String ATTR_SN = "sn";
   public static final String ATTR_MAIL = "mail";
   public static final String ATTR_ENTRYUUID = "entryUUID";
   public static final String ATTR_CN = "cn";
   public static final String ATTR_UID = "uid";
   public static final String ATTR_TELEPHONE_NUMBER = "telephoneNumber";
   public static final String ATTR_MEMBERS = "uniqueMember";
   public static final String ATTR_MEMBER_OF = "isMemberOf";
   public static final String[] ATTR_ALL = new String[]{ATTR_CN, ATTR_UID, ATTR_MAIL, ATTR_TELEPHONE_NUMBER, ATTR_ENTRYUUID, ATTR_MEMBERS, ATTR_MEMBER_OF};
   private static Map<String, LDAPConnectionPool> poolMap = new HashMap<>();

   public static String getDN(String name) {
      if (!name.contains("=")) {
         return "cn=" + name + "," + LdapConfig.getContext();
      }
      return name;
   }

   public static LDAPConnection getConnection(String userName, String password) throws LDAPException {
      String user = getDN(userName);
      LDAPConnectionPool pool;
      if (poolMap.containsKey(user)) {
         pool = poolMap.get(user);
      } else {
         LDAPConnection conn = new LDAPConnection(LdapConfig.getServer(), LdapConfig.getPort(), user, password);
         pool = new LDAPConnectionPool(conn, 100);
         pool.setMaxWaitTimeMillis(0);
         pool.setCreateIfNecessary(true);
         pool.setConnectionPoolName(user);
         poolMap.put(user, pool);
      }
      return pool.getConnection();
   }

   public static void release(LDAPConnection conn) {
      release(conn, false);
   }

   public static void release(LDAPConnection conn, Boolean defunct) {
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

   public static String getCN(String dn) {
      String[] dnParts = dn.split(",");
      return dnParts[0].substring("cn=".length());
   }

   public static void removeConnection(String user) {
      String userDN = getDN(user);
      LDAPConnectionPool pool = poolMap.get(userDN);
      if (pool != null) {
         pool.close();
      }
      poolMap.remove(userDN);
   }
}
