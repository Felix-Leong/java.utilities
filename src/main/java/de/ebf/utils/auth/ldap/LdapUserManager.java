package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.AddRequest;
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
    public LdapUser createUser(String username, String firstname, String lastname, LdapConfig config) throws AuthException {
        LDAPConnection connection = null;
        try {
            Entry entry = new Entry(LdapUtil.getDN(username, config.getBaseDN()));
            entry.addAttribute(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_USER);
            entry.addAttribute(LdapUtil.ATTR_FIRST_NAME, firstname);
            entry.addAttribute(LdapUtil.ATTR_LAST_NAME, lastname);
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(config);
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
                return getUser(username, config);
            } else {
                throw new LdapException("Adding user returned LDAP result code " + ldapResult.getResultCode());
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapUser updateUser(LdapUser user, LdapConfig oldConfig, LdapConfig newConfig) throws AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(newConfig);
            List<Modification> mods = new ArrayList<>();
            
            String newDN = LdapUtil.getDN(user.getName(), newConfig.getBaseDN());
            if (!StringUtils.isEmpty(user.getName())) {
                if (!oldConfig.getBaseDN().equals(newConfig.getBaseDN())) {

                    //get all groups for current user before renaming user. Otherwise group.getMembers() will already contain renamed users
                    List<LdapGroup> allGroups = groupManager.getGroupsForUser(user, oldConfig);

                    ModifyDNRequest modifyDNRequest = new ModifyDNRequest(user.getDN(), "cn=" + user.getName(), true, newConfig.getBaseDN());
                    LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                    if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                        throw new LdapException("Renaming user returned LDAP result code " + ldapResult.getResultCode());
                    }

                    //List<LdapGroup> allGroups = groupManager.getAllGroups(oldContext);
                    if (newConfig.getType().equals(LdapType.OpenDS)){
                        //also update all dn membership values, since OpenDS doesn't take care of this
                        for (LdapGroup ldapGroup : allGroups) {
                            Modification deleteOldUserDN = new Modification(ModificationType.DELETE, LdapUtil.ATTR_MEMBERS, user.getDN());
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
                resetPassword(user.getName(), user.getPassword(), newConfig);
            }
            if (mods.size() > 0) {
                ModifyRequest modifyRequest = new ModifyRequest(newDN, mods);
                LDAPResult ldapResult = connection.modify(modifyRequest);
                if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                    throw new LdapException("Updating user returned LDAP result code " + ldapResult.getResultCode());
                }
            }
            user = getUserByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, user.getUUID(), newConfig);
            return user;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapUser authenticate(String userName, String password, LdapConfig config) throws LdapException {
        //do not use connection pool for auth request
        LdapUser user = null;
        LDAPConnection conn = null;
        try {
            conn = new LDAPConnection(config.getServer(), config.getPort(), LdapUtil.getDN(userName, config.getBaseDN()), password);
            user = getUserByAttribute(conn, LdapUtil.ATTR_CN, userName, config);
            conn.close();
        } catch (LDAPException e) {
            throw new LdapException(e.getMessage());
        } finally {
            LdapUtil.release(conn);
        }
        return user;
    }

    @Override
    public LdapUser getUser(String userName, LdapConfig config) throws LdapException {
        return getUser(userName, config.getUsername(), config.getPassword(), config);
    }

    public LdapUser getUser(String userName, String bindName, String bindPass, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {

            if (!StringUtils.isEmpty(bindName) && !StringUtils.isEmpty(bindPass)) {
                connection = LdapUtil.getConnection(bindName, bindPass, config);
            } else {
                connection = LdapUtil.getConnection(config.getUsername(), config.getPassword(), config);
            }
            LdapUser user = getUserByAttribute(connection, LdapUtil.ATTR_CN, userName, config);
            return user;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public List<LdapUser> getAllUsers(LdapConfig config) throws LdapException {
        List<LdapUser> users = new ArrayList<>();
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Filter userFilter = Filter.createEqualityFilter(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_USER);
            users.addAll(getUsersByFilter(connection, userFilter, config));
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
        return users;
    }

    @Override
    public LdapUser resetPassword(String username, String newPassword, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            // http://msdn.microsoft.com/en-us/library/cc223248.aspx
            Modification modification = new Modification(ModificationType.REPLACE, LdapUtil.ATTR_USER_PW, newPassword);
            LDAPResult ldapResult = connection.modify(LdapUtil.getDN(username, config.getBaseDN()), modification);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                throw new LdapException("Error while resetting user password in LDAP: " + ldapResult.getResultCode());
            }
            LdapUser user = authenticate(username, newPassword, config);
            LdapUtil.removeConnection(username, config.getBaseDN());
            return user;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public boolean deleteUser(String UUID, LdapConfig config) throws LdapException, AuthException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            LdapUser user = getUserByUUID(UUID, config);
            List<LdapGroup> groups = groupManager.getGroupsForUser(user, config);
            if (groups != null) {
                for (LdapGroup group : groups) {
                    groupManager.removeUserFromGroup(user, group, config);
                }
                DeleteRequest deleteRequest = new DeleteRequest(user.getDN());
                LDAPResult ldapResult = connection.delete(deleteRequest);
                return (ldapResult.getResultCode() == ResultCode.SUCCESS);
            }
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
        return false;
    }

    public LdapUser getUserByUUID(String UUID, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            Filter filter;
            if (config.getType().equals(LdapType.ActiveDirectory)) {
                filter = Filter.createEqualityFilter(LdapUtil.ATTR_ENTRYUUID, LdapUtil.UUIDStringToByteArray(UUID));
            } else {
                filter = Filter.createEqualityFilter(LdapUtil.ATTR_ENTRYUUID, UUID);
            }
            connection = LdapUtil.getConnection(config);
            LdapUser user = getUserByFilter(connection, filter, config);
            return user;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    public List<LdapUser> getUsersByMail(String mail, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Filter filter = Filter.createEqualityFilter(LdapUtil.ATTR_MAIL, mail);
            List<LdapUser> users = getUsersByFilter(connection, filter, config);
            return users;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    private LdapUser getLdapUser(SearchResultEntry entry, LdapConfig config) throws LdapException {
        LdapUser user = new LdapUser();
        user.setName(entry.getAttributeValue(LdapUtil.ATTR_CN));
        user.setFirstName(entry.getAttributeValue(LdapUtil.ATTR_FIRST_NAME));
        user.setLastName(entry.getAttributeValue(LdapUtil.ATTR_LAST_NAME));
        user.setUid(entry.getAttributeValue(LdapUtil.ATTR_UID));
        user.setMail(entry.getAttributeValue(LdapUtil.ATTR_MAIL));
        user.setPhone(entry.getAttributeValue(LdapUtil.ATTR_TELEPHONE_NUMBER));
        if (config.getType().equals(LdapType.ActiveDirectory)) {
          //for some reason the objectGUID is stored in binary format in Active Directory
            // Microsoft stores GUIDs in a binary format that differs from the RFC standard of UUIDs (RFC #4122).
            String uuid = LdapUtil.bytesToUUID(entry.getAttributeValueBytes(LdapUtil.ATTR_ENTRYUUID));
            user.setUUID(uuid);
        } else {
            user.setUUID(entry.getAttributeValue(LdapUtil.ATTR_ENTRYUUID));
        }
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

    protected LdapUser getUserByAttribute(LDAPConnection connection, String attribute, String value, LdapConfig config) throws LdapException {
        Filter filter = Filter.createEqualityFilter(attribute, value);
        return getUserByFilter(connection, filter, config);
    }

    public List<LdapUser> getUsersByFilter(LDAPConnection connection, Filter filter, LdapConfig config) throws LdapException {
        List<LdapUser> users = new ArrayList<>();
        try {
            Filter userFilter = Filter.createEqualityFilter(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECTCLASS_USER);
            Filter searchFilter = Filter.createANDFilter(userFilter, filter);
            SearchResult searchResults = connection.search(config.getBaseDN(), SearchScope.SUB, searchFilter, LdapUtil.ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    String dn = entry.getAttributeValue(LdapUtil.ATTR_DN);
                    // do not add object from the Builtin container (Active Directory) or the LDAP agent account
                    if (!dn.contains("CN=Builtin")){
                        if (!dn.equalsIgnoreCase(config.getUsername())){
                            users.add(getLdapUser(entry, config));
                        }
                    }
                }
                Collections.sort(users);
            } 
            if (users.isEmpty()){
                log.warn("Could not find any ldap users for filter [attrName="+filter.getAttributeName()+", value="+filter.getAssertionValue()+", baseDN="+config.getBaseDN()+"]");
            }
        } catch (LDAPSearchException e) {
            throw new LdapException(e);
        }
        return users;
    }

    private LdapUser getUserByFilter(LDAPConnection connection, Filter filter, LdapConfig config) throws LdapException {
        List<LdapUser> users = getUsersByFilter(connection, filter, config);
        if (users.size() == 1) {
            return users.get(0);
        } else if (users.size() > 1) {
            log.warn("Found multiple LDAP users for filter " + filter);
        }
        return null;
    }
}
