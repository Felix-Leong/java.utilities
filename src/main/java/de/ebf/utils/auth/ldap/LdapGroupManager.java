/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.ldap.config.LdapConfig;
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
    public LdapGroup createGroup(String groupName, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            Entry entry = new Entry(LdapUtil.getDN(groupName, config.getBaseDN()));
            entry.addAttribute(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_GROUP);
            entry.addAttribute(config.getSchema().ATTR_CN, groupName);
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(config);
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
                return getGroup(groupName, config);
            } else {
                throw new LdapException("Adding group returned LDAP result code " + ldapResult.getResultCode());
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapGroup getGroup(String groupName, LdapConfig config) throws LdapException {
        Filter filter = Filter.createEqualityFilter(config.getSchema().ATTR_CN, groupName);
        LdapGroup group = getGroupByFilter(filter, config);
        return group;
    }

    public LdapGroup getGroupByUUID(String UUID, LdapConfig config) throws LdapException {
        Filter filter;
        if (config.getType().equals(LdapType.ActiveDirectory)) {
            byte[] objectGUIDByte = LdapUtil.UUIDStringToByteArray(UUID);
            filter = Filter.createEqualityFilter(config.getSchema().ATTR_ENTRYUUID, objectGUIDByte);
        } else {
            filter = Filter.createEqualityFilter(config.getSchema().ATTR_ENTRYUUID, UUID);
        }
        LdapGroup group = getGroupByFilter(filter, config);
        return group;    
    }

    @Override
    public LdapGroup updateGroup(LdapGroup group, LdapConfig oldConfig, LdapConfig newConfig) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(oldConfig);
            LdapGroup currentGroup = getGroupByUUID(group.getUUID(), oldConfig);

            if (!StringUtils.isEmpty(group.getName())) {
                if (!currentGroup.getName().equals(group.getName()) || !oldConfig.getBaseDN().equals(newConfig.getBaseDN())) {
                    ModifyDNRequest modifyDNRequest = new ModifyDNRequest(currentGroup.getDN(), "cn=" + group.getName(), true, newConfig.getBaseDN());
                    LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                    if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                        throw new LdapException("Renaming group returned LDAP result code " + ldapResult.getResultCode());
                    }
                }
            }
            group = getGroupByUUID(group.getUUID(), newConfig);
            return group;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public List<LdapGroup> getAllGroups(LdapConfig config) throws LdapException {
        List<LdapGroup> groups = new ArrayList<>();
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Filter groupFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_GROUP);
            SearchResult searchResults = null;
            try {
                searchResults = connection.search(config.getBaseDN(), SearchScope.SUB, groupFilter, config.getSchema().ATTR_ALL);
            } catch (LDAPSearchException e){
                if (!e.getResultCode().equals(ResultCode.NO_SUCH_OBJECT)){
                    throw e;
                }
            }
            
            if (searchResults!=null && searchResults.getEntryCount() > 0) {
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    String dn = entry.getAttributeValue(config.getSchema().ATTR_DN);
                    // do not add object from the Builtin container (Active Directory) 
                    if (!dn.contains("CN=Builtin")){
                        groups.add(getLdapGroup(connection, entry, config));
                    }
                }
                Collections.sort(groups);
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
        return groups;
    }

    @Override
    public Boolean deleteGroup(String UUID, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            LdapGroup group = getGroupByUUID(UUID, config);
            DeleteRequest deleteRequest = new DeleteRequest(group.getDN());
            LDAPResult ldapResult = connection.delete(deleteRequest);
            return (ldapResult.getResultCode() == ResultCode.SUCCESS);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    private List<LdapGroup> getGroupsByFilter(Filter filter, LdapConfig config) throws LdapException {
        List<LdapGroup> groups = new ArrayList<>();
        LDAPConnection conn = null;
        try {
            conn = LdapUtil.getConnection(config);
            Filter groupFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_GROUP);
            Filter searchFilter;
//            if (LdapConfig.getType().equals(LdapType.ActiveDirectory)){
//                //Filter out any groups that are within the builtin container because these cannot be moved
//                Filter builtInFilter = Filter.createSubstringFilter(LdapUtil.ATTR_DN, null, new String[]{"Builtin"}, null);
//                Filter notBuiltInFilter = Filter.createNOTFilter(builtInFilter);
//                searchFilter = Filter.createANDFilter(groupFilter, filter, notBuiltInFilter);
//            } else {
                searchFilter = Filter.createANDFilter(groupFilter, filter);
//            }
            SearchResult searchResults = conn.search(config.getBaseDN(), SearchScope.SUB, searchFilter, config.getSchema().ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                groups.add(getLdapGroup(conn, searchResults.getSearchEntries().get(0), config));
            }
        } catch (Exception e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(conn);
        }
        return groups;
    }

    private LdapGroup getGroupByFilter(Filter filter, LdapConfig config) throws LdapException {
        List<LdapGroup> groups = getGroupsByFilter(filter, config);
        if (groups.size() == 1) {
            return groups.get(0);
        } else if (groups.size() > 1) {
            log.warn("Found multiple LDAP groups for filter " + filter);
        }
        return null;
    }

    private LdapGroup getLdapGroup(LDAPConnection connection, SearchResultEntry entry, LdapConfig config) throws LdapException {
        LdapGroup group = new LdapGroup();
        group.setName(entry.getAttributeValue(config.getSchema().ATTR_CN));
        if (config.getType().equals(LdapType.ActiveDirectory)) {
            String uuid = LdapUtil.bytesToUUID(entry.getAttributeValueBytes(config.getSchema().ATTR_ENTRYUUID));
            group.setUUID(uuid);
        } else {
            group.setUUID(entry.getAttributeValue(config.getSchema().ATTR_ENTRYUUID));
        }
        group.setDN(entry.getDN());
        try {
            group.setContext(entry.getParentDNString());
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        }
        group.setMembers(getUserMembers(connection, entry, config));
        return group;
    }

    private List<LdapUser> getUserMembers(LDAPConnection connection, SearchResultEntry entry, LdapConfig config) throws LdapException {
        List<LdapUser> users = new ArrayList<>();
        Attribute attr = entry.getAttribute(config.getSchema().ATTR_MEMBERS);
        if (attr != null) {
            String[] memberDNs = attr.getValues();
            for (String dn : memberDNs) {
                LdapUser user = userManager.getUserByAttribute(connection, config.getSchema().ATTR_DN, dn, config);
                if (user == null) {
                    //members can be users or groups, so if it is not a user, check if it is a group
                    Filter groupFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_GROUP);
                    Filter dnFilter = Filter.createEqualityFilter(config.getSchema().ATTR_DN, dn);
                    Filter filter = Filter.createANDFilter(groupFilter, dnFilter);
                    try {
                        SearchResult searchResult = connection.search(config.getBaseDN(), SearchScope.SUB, filter, config.getSchema().ATTR_ALL);
                        if (searchResult.getSearchEntries() != null && searchResult.getSearchEntries().size() > 0) {
                            for (SearchResultEntry groupEntry : searchResult.getSearchEntries()) {
                                //recurse through member groups
                                users.addAll(getUserMembers(connection, groupEntry, config));
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
    public LdapGroup removeUserFromGroup(LdapUser user, LdapGroup group, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Modification modification = new Modification(ModificationType.DELETE, config.getSchema().ATTR_MEMBERS, user.getDN());
            ModifyRequest modifyRequest = new ModifyRequest(group.getDN(), modification);
            LDAPResult ldapResult = connection.modify(modifyRequest);
            if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                throw new LdapException("Removing user from group returned LDAP result code " + ldapResult.getResultCode());
            }
            return getGroup(group.getName(), config);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapGroup addUserToGroup(LdapUser user, LdapGroup group, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Modification modification = new Modification(ModificationType.ADD, config.getSchema().ATTR_MEMBERS, user.getDN());
            ModifyRequest modifyRequest = new ModifyRequest(group.getDN(), modification);
            LDAPResult ldapResult = connection.modify(modifyRequest);
            if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                throw new LdapException("Adding user to group returned LDAP result code " + ldapResult.getResultCode());
            }
            return getGroup(group.getName(), config);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    public List<LdapGroup> getGroupsForUser(LdapUser user, LdapConfig config) throws LdapException {
        List<LdapGroup> groups = getAllGroups(config);
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
