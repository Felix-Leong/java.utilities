package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
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
import de.ebf.utils.auth.AuthException;
import de.ebf.utils.auth.UserManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author dwissk
 */
@Component
public class LdapUserManager implements UserManager<LdapUser> {

   private static final Logger log = Logger.getLogger(LdapUserManager.class);
   @Autowired
   private LdapGroupManager groupManager;

    @Override
    public LdapUser createUser(String username, String firstname, String lastname, String context) throws AuthException {
        LDAPConnection connection = null;
        try {
            Entry entry = new Entry(LdapUtil.getDN(username, context));
            entry.addAttribute(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_USER);
            entry.addAttribute(LdapUtil.ATTR_FIRST_NAME, firstname);
            entry.addAttribute(LdapUtil.ATTR_LAST_NAME, lastname);
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
                return getUser(username, context);
            } else {
                throw new LdapException("Adding user returned LDAP result code " + ldapResult.getResultCode());
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public LdapUser updateUser(LdapUser user, String oldContext, String newContext) throws AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), oldContext);
            List<Modification> mods = new ArrayList<>();
            LdapUser currentUser = getUserByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, user.getUUID(), oldContext);

            String newDN = LdapUtil.getDN(user.getName(), newContext);
            if (!StringUtils.isEmpty(user.getName())) {
                if (!currentUser.getName().equals(user.getName()) || !oldContext.equals(newContext)) {

                    //get all groups for current user before renaming user. Otherwise group.getMembers() will already contain renamed users
                    List<LdapGroup> allGroups = groupManager.getGroupsForUser(user, currentUser.getContext());

                    ModifyDNRequest modifyDNRequest = new ModifyDNRequest(currentUser.getDN(), "cn=" + user.getName(), true, newContext);
                    LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                    if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                        throw new LdapException("Renaming user returned LDAP result code " + ldapResult.getResultCode());
                    }

               //List<LdapGroup> allGroups = groupManager.getAllGroups(oldContext);
                    //also update all dn membership values, since LDAP doesn't take care of this
                    for (LdapGroup ldapGroup : allGroups) {
                        Modification deleteOldUserDN = new Modification(ModificationType.DELETE, LdapUtil.ATTR_MEMBERS, currentUser.getDN());
                        Modification addNewUserDN = new Modification(ModificationType.ADD, LdapUtil.ATTR_MEMBERS, newDN);
                        List<Modification> groupMods = new ArrayList<>();
                        groupMods.add(deleteOldUserDN);
                        groupMods.add(addNewUserDN);
                        ModifyRequest modifyRequest = new ModifyRequest(ldapGroup.getDN(), groupMods);
                        ldapResult = connection.modify(modifyRequest);
                        if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                            throw new LdapException("Updating user in group returned LDAP result code " + ldapResult.getResultCode());
                        }
                    }
                }
            }
            if (!StringUtils.isEmpty(user.getFirstName())) {
                mods.add(new Modification(ModificationType.REPLACE, LdapUtil.ATTR_FIRST_NAME, user.getFirstName()));
            }

            if (!StringUtils.isEmpty(user.getLastName())) {
                mods.add(new Modification(ModificationType.REPLACE, LdapUtil.ATTR_LAST_NAME, user.getLastName()));
            }

            if (!StringUtils.isEmpty(user.getMail())) {
                mods.add(new Modification(ModificationType.REPLACE, LdapUtil.ATTR_MAIL, user.getMail()));
            }

            if (!StringUtils.isEmpty(user.getPassword())) {
                resetPassword(currentUser.getName(), user.getPassword(), newContext);
            }
            if (mods.size() > 0) {
                ModifyRequest modifyRequest = new ModifyRequest(newDN, mods);
                LDAPResult ldapResult = connection.modify(modifyRequest);
                if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                    throw new LdapException("Updating user returned LDAP result code " + ldapResult.getResultCode());
                }
            }
            user = getUserByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, user.getUUID(), newContext);
            return user;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

   @Override
   public LdapUser authenticate(String userName, String password, String context) throws LdapException {
      //do not use connection pool for auth request
      LdapUser user = null;
      try {
         LDAPConnection conn = new LDAPConnection(LdapConfig.getServer(), LdapConfig.getPort(), LdapUtil.getDN(userName, context), password);
         user = getUserByAttribute(conn, LdapUtil.ATTR_CN, userName, context);
         conn.close();
      } catch (LDAPException e) {
         throw new LdapException(e.getMessage());
      }
      return user;
   }

   @Override
   public LdapUser getUser(String userName, String context) throws LdapException {
      return getUser(userName, LdapConfig.getUser(), LdapConfig.getPass(), context);
   }

    public LdapUser getUser(String userName, String bindName, String bindPass, String context) throws LdapException {
        LDAPConnection connection = null;
        try {

            if (!StringUtils.isEmpty(bindName) && !StringUtils.isEmpty(bindPass)) {
                connection = LdapUtil.getConnection(bindName, bindPass, context);
            } else {
                connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            }
            LdapUser user = getUserByAttribute(connection, LdapUtil.ATTR_CN, userName, context);
            return user;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public List<LdapUser> getAllUsers(String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            Filter userFilter = Filter.create(LdapUtil.ATTR_OBJECTCLASS + "=" + LdapUtil.OBJECTCLASS_USER);
            SearchResult searchResults = connection.search(context, SearchScope.SUB, userFilter, LdapUtil.ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                List<LdapUser> users = new ArrayList<>();
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    users.add(getLdapUser(entry));
                }
                Collections.sort(users);
                return users;
            } else {
                log.warn("Could not find any ldap users");
                return new ArrayList<>();
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public LdapUser resetPassword(String username, String newPassword, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            Modification modification = new Modification(ModificationType.REPLACE, "userPassword", newPassword);
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LDAPResult ldapResult = connection.modify(LdapUtil.getDN(username, context), modification);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                throw new LdapException("Error while resetting user password in LDAP: " + ldapResult.getResultCode());
            }
            LdapUser user = authenticate(username, newPassword, context);
            LdapUtil.removeConnection(username, context);
            return user;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public boolean deleteUser(String UUID, String context) throws LdapException, AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LdapUser user = getUserByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, UUID, context);
            List<LdapGroup> groups = groupManager.getGroupsForUser(user, context);
            if (groups != null) {
                for (LdapGroup group : groups) {
                    groupManager.removeUserFromGroup(user, group, context);
                }
                DeleteRequest deleteRequest = new DeleteRequest(user.getDN());
                LDAPResult ldapResult = connection.delete(deleteRequest);
                return (ldapResult.getResultCode() == ResultCode.SUCCESS);
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
        return false;
    }

    
    public LdapUser getUserByUUID(String UUID, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LdapUser user = getUserByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, UUID, context);
            return user;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    public List<LdapUser> getUsersByMail(String mail) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), LdapConfig.getContext());
            List<LdapUser> users = getUsersByAttribute(connection, LdapUtil.ATTR_MAIL, mail, LdapConfig.getContext());
            return users;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

   private LdapUser getLdapUser(SearchResultEntry entry) throws LdapException {
      LdapUser user = new LdapUser();
      user.setName(entry.getAttributeValue(LdapUtil.ATTR_CN));
      user.setFirstName(entry.getAttributeValue(LdapUtil.ATTR_FIRST_NAME));
      user.setLastName(entry.getAttributeValue(LdapUtil.ATTR_LAST_NAME));
      user.setUid(entry.getAttributeValue(LdapUtil.ATTR_UID));
      user.setMail(entry.getAttributeValue(LdapUtil.ATTR_MAIL));
      user.setPhone(entry.getAttributeValue(LdapUtil.ATTR_TELEPHONE_NUMBER));
      user.setUUID(entry.getAttributeValue(LdapUtil.ATTR_ENTRYUUID));
      user.setDN(entry.getDN());
      try {
         user.setContext(entry.getParentDNString());
      } catch (LDAPException ex) {
         throw new LdapException(ex);
      }

// isMemberOf does not seem to work in OpenDS
//      Attribute attr = entry.getAttribute(LdapUtil.ATTR_MEMBER_OF);
//      if (attr != null) {
//         String[] groupDNs = attr.getValues();
//         List<LdapGroup> groups = new ArrayList<>(groupDNs.length);
//         for (int i = 0; i < groupDNs.length; i++) {
//            LdapGroup ldapGroup = groupManager.getGroup(LdapUtil.getCN(groupDNs[i]));
//            groups.add(ldapGroup);
//         }
//         user.setGroups(groups);
//      }
      return user;
   }

   protected List<LdapUser> getUsersByAttribute(LDAPConnection connection, String attribute, String value, String context) throws LdapException {
      return getUsersByFilter(connection, "(" + attribute + "=" + value + ")", context);
   }

   protected LdapUser getUserByAttribute(LDAPConnection connection, String attribute, String value, String context) throws LdapException {
      return getUserByFilter(connection, "(" + attribute + "=" + value + ")", context);
   }

   public List<LdapUser> getUsersByFilter(LDAPConnection connection, String filter, String context) throws LdapException {
      List<LdapUser> users = new ArrayList<>();
      try {
         SearchResult searchResults = connection.search(context, SearchScope.SUB, filter, LdapUtil.ATTR_ALL);
         if (searchResults.getEntryCount() > 0) {
            users.add(getLdapUser(searchResults.getSearchEntries().get(0)));
         }
      } catch (LDAPException e) {
         throw new LdapException(e);
      }
      return users;
   }

   public LdapUser getUserByFilter(LDAPConnection connection, String filter, String context) throws LdapException {
      List<LdapUser> users = getUsersByFilter(connection, filter, context);
      if (users.size() == 1) {
         return users.get(0);
      } else if (users.size() > 1) {
         log.warn("Found multiple LDAP users for filter " + filter);
      }
      return null;
   }
}
