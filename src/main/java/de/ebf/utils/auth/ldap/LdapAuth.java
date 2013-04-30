package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyDNRequest;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import de.ebf.utils.auth.Auth;
import de.ebf.utils.auth.AuthException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author dwissk
 */
@Component
public class LdapAuth implements Auth<LdapUser> {

   private static final Logger log = Logger.getLogger(LdapAuth.class);
   private static final String ATTR_USER_PW = "userPassword";
   private static final String ATTR_OBJECTCLASS = "objectclass";
   private static final String ATTR_OBJECTCLASS_INET_ORG_PERSON = "inetOrgPerson";
   private static final String ATTR_SN = "sn";
   private static final String ATTR_MAIL = "mail";
   private static final String ATTR_ENTRYUUID = "entryUUID";
   private static final String ATTR_CN = "cn";
   private static final String ATTR_UID = "uid";
   private static final String ATTR_TELEPHONE_NUMBER = "telephoneNumber";
   private static final String[] ATTR_ALL = new String[]{"cn", "uid", "mail", "telephoneNumber", "entryUUID", "entryDN"};

   @Override
   public LdapUser createUser(String username) throws AuthException {
      try {
         Entry entry = new Entry(getDN(username));
         entry.addAttribute(ATTR_OBJECTCLASS, ATTR_OBJECTCLASS_INET_ORG_PERSON);
         entry.addAttribute(ATTR_SN, username);
         AddRequest addRequest = new AddRequest(entry);
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         LDAPResult ldapResult = connection.add(addRequest);
         if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
            return getUser(username);
         } else {
            throw new LdapAuthException("Adding user returned LDAP result code " + ldapResult.getResultCode());
         }
      } catch (LDAPException e) {
         throw new LdapAuthException(e);
      }
   }

   @Override
   public LdapUser updateUser(LdapUser user) throws AuthException {
      try {
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         List<Modification> mods = new ArrayList<>();
         LdapUser currentUser = getUserByAttribute(connection, ATTR_ENTRYUUID, user.getUUID());

         if (!StringUtils.isEmpty(user.getName())) {
            if (!currentUser.getName().equals(user.getName())) {
               ModifyDNRequest modifyDNRequest = new ModifyDNRequest(currentUser.getDN(), "cn=" + user.getName(), true);
               LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
               if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                  throw new LdapAuthException("Renaming user returned LDAP result code " + ldapResult.getResultCode());
               }
            }
         }
         if (!StringUtils.isEmpty(user.getMail())) {
            mods.add(new Modification(ModificationType.REPLACE, ATTR_MAIL, user.getMail()));
         }

         if (!StringUtils.isEmpty(user.getPassword())) {
            resetPassword(currentUser.getName(), user.getPassword());
         }
         if (mods.size() > 0) {
            ModifyRequest modifyRequest = new ModifyRequest(currentUser.getDN(), mods);
            LDAPResult ldapResult = connection.modify(modifyRequest);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
               throw new LdapAuthException("Updating user returned LDAP result code " + ldapResult.getResultCode());
            }
         }
         return getUserByAttribute(connection, ATTR_ENTRYUUID, user.getUUID());
      } catch (LDAPException e) {
         throw new LdapAuthException(e);
      }
   }

   @Override
   public LdapUser authenticate(String userName, String password) throws LdapAuthException {
      return getUser(userName, userName, password);
   }

   @Override
   public LdapUser getUser(String userName) throws LdapAuthException {
      return getUser(userName, LDAPConfig.getUser(), LDAPConfig.getPass());
   }

   @Override
   public List<LdapUser> getAllUsers() throws LdapAuthException {
      try {
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         SearchResult searchResults = connection.search(LDAPConfig.getContext(), SearchScope.SUB, "(CN=*)", ATTR_ALL);
         if (searchResults.getEntryCount() > 0) {
            List<LdapUser> users = new ArrayList<>();
            for (SearchResultEntry entry : searchResults.getSearchEntries()) {
               users.add(getLdapUser(entry));
            }
            return users;
         } else {
            throw new LdapAuthException("Could not find an entry that matches given criteria.");
         }
      } catch (LDAPException e) {
         throw new LdapAuthException(e);
      }
   }

   @Override
   public LdapUser resetPassword(String username, String newPassword) throws LdapAuthException {
      try {
         Modification modification = new Modification(ModificationType.REPLACE, "userPassword", newPassword);
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         LDAPResult ldapResult = connection.modify(getDN(username), modification);
         if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
            throw new LdapAuthException("Error while resetting user password in LDAP: " + ldapResult.getResultCode());
         }
         return authenticate(username, newPassword);
      } catch (LDAPException e) {
         throw new LdapAuthException(e);
      }
   }

   @Override
   public boolean deleteUser(String UUID) throws LdapAuthException {
      try {
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         LdapUser user = getUserByAttribute(connection, ATTR_ENTRYUUID, UUID);
         DeleteRequest deleteRequest = new DeleteRequest(user.getDN());
         LDAPResult ldapResult = connection.delete(deleteRequest);
         return (ldapResult.getResultCode() == ResultCode.SUCCESS);
      } catch (LDAPException e) {
         throw new LdapAuthException(e);
      }
   }

   private LDAPConnection getConnection(String userName, String password) throws LDAPException {
      log.info("Connecting to LDAP [user=" + userName + ", server=" + LDAPConfig.getServer() + ",port=" + LDAPConfig.getPort() + ", ldapContext = " + LDAPConfig.getContext() + "]");
      String user = (userName.startsWith("cn=") ? userName : "cn=" + userName + "," + LDAPConfig.getContext());
      return new LDAPConnection(LDAPConfig.getServer(), LDAPConfig.getPort(), user, password);
   }

   private String getDN(String userName) {
      return "cn=" + userName + "," + LDAPConfig.getContext();
   }

   private LdapUser getUser(String userName, String bindName, String bindPass) throws LdapAuthException {
      try {
         LDAPConnection connection;
         if (!StringUtils.isEmpty(bindName) && !StringUtils.isEmpty(bindPass)) {
            connection = getConnection(bindName, bindPass);
         } else {
            connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         }
         return getUserByAttribute(connection, ATTR_CN, userName);
      } catch (LDAPException ex) {
         throw new LdapAuthException(ex);
      }
   }

   private LdapUser getLdapUser(SearchResultEntry entry) {
      LdapUser user = new LdapUser();
      user.setName(entry.getAttributeValue(ATTR_CN));
      user.setUid(entry.getAttributeValue(ATTR_UID));
      user.setMail(entry.getAttributeValue(ATTR_MAIL));
      user.setPhone(entry.getAttributeValue(ATTR_TELEPHONE_NUMBER));
      user.setUUID(entry.getAttributeValue(ATTR_ENTRYUUID));
      user.setDN(entry.getDN());
      return user;
   }

   private LdapUser getUserByAttribute(LDAPConnection connection, String attribute, String value) throws LdapAuthException {
      try {
         SearchResult searchResults = connection.search(LDAPConfig.getContext(), SearchScope.SUB, "(" + attribute + "=" + value + ")", ATTR_ALL);
         if (searchResults.getEntryCount() == 1) {
            return getLdapUser(searchResults.getSearchEntries().get(0));
         } else {
            throw new LdapAuthException("Unexpected number of LDAP search results: " + searchResults.getEntryCount());
         }
      } catch (LDAPException e) {
         throw new LdapAuthException(e);
      }
   }
}
