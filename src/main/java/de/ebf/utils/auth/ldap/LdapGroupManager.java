/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
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
import de.ebf.utils.auth.GroupManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Dominik
 */
@Component
public class LdapGroupManager implements GroupManager<LdapGroup, LdapUser> {

   @Autowired
   LdapUserManager userManager;
   private static final Logger log = Logger.getLogger(LdapGroupManager.class);

    @Override
    public LdapGroup createGroup(String groupName, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            Entry entry = new Entry(LdapUtil.getDN(groupName, context));
            entry.addAttribute(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_GROUP);
            entry.addAttribute(LdapUtil.ATTR_CN, groupName);
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
                return getGroup(groupName, context);
            } else {
                throw new LdapException("Adding group returned LDAP result code " + ldapResult.getResultCode());
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
    public LdapGroup getGroup(String groupName, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LdapGroup group = getGroupByAttribute(connection, LdapUtil.ATTR_CN, groupName, context);
            return group;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    public LdapGroup getGroupByUUID(String UUID, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LdapGroup group = getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, UUID, context);
            return group;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public LdapGroup updateGroup(LdapGroup group, String oldContext, String newContext) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), oldContext);
            LdapGroup currentGroup = getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, group.getUUID(), oldContext);

            if (!StringUtils.isEmpty(group.getName())) {
                if (!currentGroup.getName().equals(group.getName()) || !oldContext.equals(newContext)) {
                    ModifyDNRequest modifyDNRequest = new ModifyDNRequest(currentGroup.getDN(), "cn=" + group.getName(), true, newContext);
                    LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                    if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                        throw new LdapException("Renaming group returned LDAP result code " + ldapResult.getResultCode());
                    }
                }
            }
            group = getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, group.getUUID(), newContext);
            return group;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public List<LdapGroup> getAllGroups(String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            Filter groupFilter = Filter.create("objectClass=" + LdapUtil.OBJECTCLASS_GROUP);
            SearchResult searchResults = connection.search(context, SearchScope.SUB, groupFilter, LdapUtil.ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                List<LdapGroup> groups = new ArrayList<>();
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    groups.add(getLdapGroup(connection, entry, context));
                }
                Collections.sort(groups);
                return groups;
            } else {
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
    public Boolean deleteGroup(String UUID, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            LdapGroup group = getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, UUID, context);
            DeleteRequest deleteRequest = new DeleteRequest(group.getDN());
            LDAPResult ldapResult = connection.delete(deleteRequest);
            return (ldapResult.getResultCode() == ResultCode.SUCCESS);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    private LdapGroup getGroupByAttribute(LDAPConnection connection, String attribute, String value, String context) throws LdapException {
        boolean bReleaseLDAPConnection = true;
        try {
            SearchResult searchResults = connection.search(context, SearchScope.SUB, "(" + attribute + "=" + value + ")", LdapUtil.ATTR_ALL);
            if (searchResults.getEntryCount() == 1) {
                bReleaseLDAPConnection = false;
                return getLdapGroup(connection, searchResults.getSearchEntries().get(0), context);
            } else {
                log.warn("Unexpected number of LDAP search results: " + searchResults.getEntryCount());
                return null;
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (bReleaseLDAPConnection) {
                LdapUtil.release(connection);
            }
        }
    }

   private LdapGroup getLdapGroup(LDAPConnection connection, SearchResultEntry entry, String context) throws LdapException {
      LdapGroup group = new LdapGroup();
      group.setName(entry.getAttributeValue(LdapUtil.ATTR_CN));
      group.setUUID(entry.getAttributeValue(LdapUtil.ATTR_ENTRYUUID));
      group.setDN(entry.getDN());
      try {
         group.setContext(entry.getParentDNString());
      } catch (LDAPException ex) {
         throw new LdapException(ex);
      }
      Attribute attr = entry.getAttribute(LdapUtil.ATTR_MEMBERS);
      if (attr != null) {
         String[] userDNs = attr.getValues();
         List<LdapUser> users = new ArrayList<>(userDNs.length);
         for (int i = 0; i < userDNs.length; i++) {
            String cn = LdapUtil.getCN(userDNs[i]);
            LdapUser user = userManager.getUserByAttribute(connection, "cn", cn, group.getContext());
            if (user != null) {
               users.add(user);
            }
         }
         group.setMembers(users);
      }
      return group;
   }

    @Override
    public LdapGroup removeUserFromGroup(LdapUser user, LdapGroup group, String context) throws AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            Modification modification = new Modification(ModificationType.DELETE, LdapUtil.ATTR_MEMBERS, user.getDN());
            ModifyRequest modifyRequest = new ModifyRequest(group.getDN(), modification);
            LDAPResult ldapResult = connection.modify(modifyRequest);
            if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                throw new LdapException("Removing user from group returned LDAP result code " + ldapResult.getResultCode());
            }
            return getGroup(group.getName(), context);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public LdapGroup addUserToGroup(LdapUser user, LdapGroup group, String context) throws AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), context);
            Modification modification = new Modification(ModificationType.ADD, LdapUtil.ATTR_MEMBERS, user.getDN());
            ModifyRequest modifyRequest = new ModifyRequest(group.getDN(), modification);
            LDAPResult ldapResult = connection.modify(modifyRequest);
            if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                throw new LdapException("Adding user to group returned LDAP result code " + ldapResult.getResultCode());
            }
            return getGroup(group.getName(), context);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

   public List<LdapGroup> getGroupsForUser(LdapUser user, String context) throws LdapException {
      List<LdapGroup> groups = getAllGroups(context);
      Iterator<LdapGroup> it = groups.iterator();
      while (it.hasNext()) {
         LdapGroup group = it.next();
         if (!group.getMembers().contains(user)) {
            it.remove();
         }
      }
      return groups;
   }
}
