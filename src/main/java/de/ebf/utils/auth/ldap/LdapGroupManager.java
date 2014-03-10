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
import com.unboundid.ldap.sdk.LDAPSearchException;
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
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
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
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
            Filter filter = Filter.createEqualityFilter(LdapUtil.ATTR_CN, groupName);
            LdapGroup group = getGroupByFilter(connection, filter, context);
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
            Filter filter;
            if (LdapDefaultConfig.getType().equals(LdapType.ActiveDirectory)) {
                byte[] objectGUIDByte = LdapUtil.UUIDStringToByteArray(UUID);
                filter = Filter.createEqualityFilter(LdapUtil.ATTR_ENTRYUUID, objectGUIDByte);
            } else {
                filter = Filter.createEqualityFilter(LdapUtil.ATTR_ENTRYUUID, UUID);
            }
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
            LdapGroup group = getGroupByFilter(connection, filter, context);
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
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), oldContext);
            LdapGroup currentGroup = getGroupByUUID(group.getUUID(), oldContext);

            if (!StringUtils.isEmpty(group.getName())) {
                if (!currentGroup.getName().equals(group.getName()) || !oldContext.equals(newContext)) {
                    ModifyDNRequest modifyDNRequest = new ModifyDNRequest(currentGroup.getDN(), "cn=" + group.getName(), true, newContext);
                    LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                    if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                        throw new LdapException("Renaming group returned LDAP result code " + ldapResult.getResultCode());
                    }
                }
            }
            group = getGroupByUUID(group.getUUID(), newContext);
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
        List<LdapGroup> groups = new ArrayList<>();
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
            Filter groupFilter = Filter.createEqualityFilter(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_GROUP);
            SearchResult searchResults = connection.search(context, SearchScope.SUB, groupFilter, LdapUtil.ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    String dn = entry.getAttributeValue(LdapUtil.ATTR_DN);
                    // do not add object from the Builtin container (Active Directory) 
                    if (!dn.contains("CN=Builtin")){
                        groups.add(getLdapGroup(connection, entry, context));
                    }
                }
                Collections.sort(groups);
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
        return groups;
    }

    @Override
    public Boolean deleteGroup(String UUID, String context) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
            LdapGroup group = getGroupByUUID(UUID, context);
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

    private List<LdapGroup> getGroupsByFilter(LDAPConnection connection, Filter filter, String context) throws LdapException {
        List<LdapGroup> groups = new ArrayList<>();
        try {
            Filter groupFilter = Filter.createEqualityFilter(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_GROUP);
            Filter searchFilter;
//            if (LdapConfig.getType().equals(LdapType.ActiveDirectory)){
//                //Filter out any groups that are within the builtin container because these cannot be moved
//                Filter builtInFilter = Filter.createSubstringFilter(LdapUtil.ATTR_DN, null, new String[]{"Builtin"}, null);
//                Filter notBuiltInFilter = Filter.createNOTFilter(builtInFilter);
//                searchFilter = Filter.createANDFilter(groupFilter, filter, notBuiltInFilter);
//            } else {
                searchFilter = Filter.createANDFilter(groupFilter, filter);
//            }
            SearchResult searchResults = connection.search(context, SearchScope.SUB, searchFilter, LdapUtil.ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                groups.add(getLdapGroup(connection, searchResults.getSearchEntries().get(0), context));
            }
        } catch (LDAPSearchException e) {
            throw new LdapException(e);
        }
        return groups;
    }

    public LdapGroup getGroupByFilter(LDAPConnection connection, Filter filter, String context) throws LdapException {
        List<LdapGroup> groups = getGroupsByFilter(connection, filter, context);
        if (groups.size() == 1) {
            return groups.get(0);
        } else if (groups.size() > 1) {
            log.warn("Found multiple LDAP groups for filter " + filter);
        }
        return null;
    }

    private LdapGroup getLdapGroup(LDAPConnection connection, SearchResultEntry entry, String context) throws LdapException {
        LdapGroup group = new LdapGroup();
        group.setName(entry.getAttributeValue(LdapUtil.ATTR_CN));
        if (LdapDefaultConfig.getType().equals(LdapType.ActiveDirectory)) {
            String uuid = LdapUtil.bytesToUUID(entry.getAttributeValueBytes(LdapUtil.ATTR_ENTRYUUID));
            group.setUUID(uuid);
        } else {
            group.setUUID(entry.getAttributeValue(LdapUtil.ATTR_ENTRYUUID));
        }
        group.setDN(entry.getDN());
        try {
            group.setContext(entry.getParentDNString());
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        }
        group.setMembers(getUserMembers(connection, entry, context));
        return group;
    }

    private List<LdapUser> getUserMembers(LDAPConnection connection, SearchResultEntry entry, String context) throws LdapException {
        List<LdapUser> users = new ArrayList<>();
        Attribute attr = entry.getAttribute(LdapUtil.ATTR_MEMBERS);
        if (attr != null) {
            String[] memberDNs = attr.getValues();
            for (String dn : memberDNs) {
                LdapUser user = userManager.getUserByAttribute(connection, LdapUtil.ATTR_DN, dn, context);
                if (user == null) {
                    //members can be users or groups, so if it is not a user, check if it is a group
                    Filter groupFilter = Filter.createEqualityFilter(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_GROUP);
                    Filter dnFilter = Filter.createEqualityFilter(LdapUtil.ATTR_DN, dn);
                    Filter filter = Filter.createANDFilter(groupFilter, dnFilter);
                    try {
                        SearchResult searchResult = connection.search(context, SearchScope.SUB, filter, LdapUtil.ATTR_ALL);
                        if (searchResult.getSearchEntries() != null && searchResult.getSearchEntries().size() > 0) {
                            for (SearchResultEntry groupEntry : searchResult.getSearchEntries()) {
                                //recurse through member groups
                                users.addAll(getUserMembers(connection, groupEntry, context));
                            }
                        }
                    } catch (LDAPSearchException e) {
                        throw new LdapException(e);
                    }
                } else {
                    users.add(user);
                }
            }
        }       
        return users;
    }

    @Override
    public LdapGroup removeUserFromGroup(LdapUser user, LdapGroup group, String context) throws AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
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
            connection = LdapUtil.getConnection(LdapDefaultConfig.getUser(), LdapDefaultConfig.getPass(), context);
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
