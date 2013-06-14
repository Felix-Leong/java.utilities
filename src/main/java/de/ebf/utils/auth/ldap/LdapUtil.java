/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
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

   public static String getDN(String name) {
      return "cn=" + name + "," + LdapConfig.getContext();
   }

   public static LDAPConnection getConnection(String userName, String password) throws LDAPException {
      log.info("Connecting to LDAP [user=" + userName + ", server=" + LdapConfig.getServer() + ",port=" + LdapConfig.getPort() + ", ldapContext = " + LdapConfig.getContext() + "]");
      String user = (userName.startsWith("cn=") ? userName : "cn=" + userName + "," + LdapConfig.getContext());
      return new LDAPConnection(LdapConfig.getServer(), LdapConfig.getPort(), user, password);
   }

   public static String getCN(String dn) {
      String[] dnParts = dn.split(",");
      return dnParts[0].substring("cn=".length());
   }
}
