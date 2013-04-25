package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import de.ebf.utils.auth.Auth;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author dwissk
 */
public class LdapAuth implements Auth {

   private static final Logger log = Logger.getLogger(LdapAuth.class);
   private static final String ATTR_USER_PW = "userPassword";

   @Override
   public LdapUser createOrUpdateUser(String username, String password) throws RuntimeException {
      try {
         Attribute attrPass = new Attribute(ATTR_USER_PW, password);
         AddRequest addRequest = new AddRequest(getDN(username), attrPass);
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         LDAPResult ldapResult = connection.add(addRequest);
         return authenticate(username, password);
      } catch (LDAPException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public LdapUser authenticate(String userName, String password) throws IllegalArgumentException {
      return getUser(userName, userName, password);
   }

   @Override
   public LdapUser getUser(String userName) throws IllegalArgumentException {
      return getUser(userName, LDAPConfig.getUser(), LDAPConfig.getPass());
   }

   @Override
   public List<LdapUser> getAllUsers() {
      try {
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         SearchResult searchResults = connection.search(LDAPConfig.getContext(), SearchScope.SUB, "(CN=*)", SearchRequest.ALL_USER_ATTRIBUTES);
         if (searchResults.getEntryCount() > 0) {
            List<LdapUser> users = new ArrayList<>();
            for (SearchResultEntry entry : searchResults.getSearchEntries()) {
               users.add(getLdapUser(entry));
            }
            return users;
         } else {
            throw new IllegalArgumentException("Could not find an entry that matches given criteria.");
         }
      } catch (LDAPException e) {
         throw new IllegalArgumentException(e);
      }
   }

   @Override
   public LdapUser resetPassword(String username, String newPassword) throws IllegalArgumentException {
      try {
         Modification modification = new Modification(ModificationType.REPLACE, "userPassword", newPassword);
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         LDAPResult ldapResult = connection.modify(getDN(username), modification);
         return authenticate(username, newPassword);
      } catch (LDAPException e) {
         throw new IllegalArgumentException(e);
      }
   }

   @Override
   public boolean deleteUser(String username) throws IllegalArgumentException {
      try {
         LdapUser user = getUser(username);
         DeleteRequest deleteRequest = new DeleteRequest(user.getUserDN());
         LDAPConnection connection = getConnection(LDAPConfig.getUser(), LDAPConfig.getPass());
         LDAPResult ldapResult = connection.delete(deleteRequest);
         return true;
      } catch (LDAPException e) {
         throw new IllegalArgumentException(e);
      }
   }

   private LDAPConnection getConnection(String userName, String password) throws LDAPException {
      log.info("Connecting to LDAP [user=" + userName + ", server=" + LDAPConfig.getServer() + ", ldapContext=" + LDAPConfig.getContext() + "]");
      String user = (userName.startsWith("cn=") ? userName : "cn=" + userName + "," + LDAPConfig.getContext());
      return new LDAPConnection(LDAPConfig.getServer(), 389, user, password);
   }

   private String getDN(String userName) {
      return "cn=" + userName + "," + LDAPConfig.getContext();
   }

   private LdapUser getUser(String userName, String bindName, String bindPass) {
      try {
         LDAPConnection connection = getConnection(bindName, bindPass);
         SearchResult searchResults = connection.search(LDAPConfig.getContext(), SearchScope.SUB, "(CN=" + userName + ")", SearchRequest.ALL_USER_ATTRIBUTES);
         if (searchResults.getEntryCount() > 0) {
            return getLdapUser(searchResults.getSearchEntries().get(0));
         } else {
            throw new IllegalArgumentException("Could not find an entry that matches given criteria.");
         }
      } catch (LDAPException e) {
         throw new IllegalArgumentException(e);
      }
   }

   private LdapUser getLdapUser(SearchResultEntry entry) {
      LdapUser user = new LdapUser();
      user.setName(entry.getAttributeValue("uid"));
      user.setMail(entry.getAttributeValue("mail"));
      user.setPhone(entry.getAttributeValue("telephoneNumber"));
      user.setUUID(entry.getAttributeValue("entryUUID"));
      user.setUserDN(entry.getAttributeValue("entryDN"));
      return user;
   }
}
