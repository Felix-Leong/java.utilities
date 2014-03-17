package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.ldap.config.LdapConfig;
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
import de.ebf.utils.Bundle;
import de.ebf.utils.auth.UserManager;
import java.io.UnsupportedEncodingException;
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
    public LdapUser createUser(LdapUser user, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            Entry entry = new Entry("cn="+user.getName()+","+config.getBaseDN());
            entry.addAttribute(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_USER);
            entry.addAttribute(config.getSchema().ATTR_FIRST_NAME, user.getFirstName());
            entry.addAttribute(config.getSchema().ATTR_LAST_NAME, user.getLastName());
            entry.addAttribute(config.getSchema().ATTR_MAIL, user.getMail());
            if (config.getType().equals(LdapType.ActiveDirectory)){
                //by default, newly created AD accounts are disabled
                //http://www.netvision.com/ad_useraccountcontrol.php?blog
                entry.addAttribute(config.getSchema().ATTR_USER_ACCOUNT_CONTROL, "544");
                entry.addAttribute(config.getSchema().ATTR_USER_PW, getActiveDirectoryPassword(user.getPassword()));
            } else {
                
            }
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(config);
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
                return getUser(user.getName(), config);
            } else {
                throw new LdapException("Adding user returned LDAP result code " + ldapResult.getResultCode());
            }
        } catch (LDAPException e) {
            if (e.getResultCode().equals(ResultCode.UNWILLING_TO_PERFORM)){
                throw new LdapException("The LDAP backend is unwilling to set the password. Please make sure that the password meets the minimum complexity requirements. Contact your LDAP administrator if you are unsure.");
            }
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapUser updateUser(LdapUser user, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            LdapUser oldUser = getUserByUUID(user.getUUID(), config);
            connection = LdapUtil.getConnection(config);
            List<Modification> mods = new ArrayList<>();
            
            String oldDN = oldUser.getDN();
            String newDN = "cn="+user.getName()+","+config.getBaseDN();
            if (!StringUtils.isEmpty(user.getName())) {
                if (!oldDN.equalsIgnoreCase(newDN)) {

                    //get all groups for current user before renaming user. Otherwise group.getMembers() will already contain renamed users
                    List<LdapGroup> allGroups = groupManager.getGroupsForUser(oldUser, config);
                    
                    ModifyDNRequest modifyDNRequest = new ModifyDNRequest(oldUser.getDN(), "cn=" + user.getName(), true, config.getBaseDN());
                    LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                    if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                        throw new LdapException("Renaming user returned LDAP result code " + ldapResult.getResultCode());
                    }

                    //List<LdapGroup> allGroups = groupManager.getAllGroups(oldContext);
                    if (config.getType().equals(LdapType.OpenDS)){
                        //also update all dn membership values, since OpenDS doesn't take care of this
                        for (LdapGroup ldapGroup : allGroups) {
                            Modification deleteOldUserDN = new Modification(ModificationType.DELETE, config.getSchema().ATTR_MEMBERS, user.getDN());
                            Modification addNewUserDN = new Modification(ModificationType.ADD, config.getSchema().ATTR_MEMBERS, newDN);
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
                mods.add(new Modification(ModificationType.REPLACE, config.getSchema().ATTR_FIRST_NAME, user.getFirstName()));
            }

            if (!StringUtils.isEmpty(user.getLastName())) {
                mods.add(new Modification(ModificationType.REPLACE, config.getSchema().ATTR_LAST_NAME, user.getLastName()));
            }

            if (!StringUtils.isEmpty(user.getMail())) {
                mods.add(new Modification(ModificationType.REPLACE, config.getSchema().ATTR_MAIL, user.getMail()));
            }

            if (mods.size() > 0) {
                ModifyRequest modifyRequest = new ModifyRequest(newDN, mods);
                LDAPResult ldapResult = connection.modify(modifyRequest);
                if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                    throw new LdapException("Updating user returned LDAP result code " + ldapResult.getResultCode());
                }
            }
            
            if (!StringUtils.isEmpty(user.getPassword())) {
                resetPassword(user, user.getPassword(), config);
            }
            
            return getUserByUUID(user.getUUID(), config);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapUser authenticate(String userName, String password, LdapConfig config) throws LdapException {
        //get distinguished name from LDAP
        LDAPConnection conn = null;
        LdapUser user;
        try {
            user = getUser(userName, config);
            if (user == null){
                throw new LdapException(Bundle.getString("InvalidUsernameOrPassword"));
            }
            //do not use connection pool for auth request
            conn = LdapUtil.getUnpooledConnection(user.getDN(), password, config);
            return user;
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            if (conn!=null){
                conn.close();
            }
        }
    }

    @Override
    public LdapUser getUser(String userName, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config.getUsername(), config.getPassword(), config);
            LdapUser user = getUserByAttribute(connection, config.getSchema().ATTR_CN, userName, config);
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
            Filter userFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_USER);
            users.addAll(getUsersByFilter(connection, userFilter, config));
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
        return users;
    }

    @Override
    public LdapUser resetPassword(LdapUser user, String newPassword, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Modification modification;
            if (config.getType().equals(LdapType.ActiveDirectory)){
                modification = new Modification(ModificationType.REPLACE, config.getSchema().ATTR_USER_PW, getActiveDirectoryPassword(newPassword));
            } else {
                modification = new Modification(ModificationType.REPLACE, config.getSchema().ATTR_USER_PW, newPassword);          
            }
            LDAPResult ldapResult = connection.modify(user.getDN(), modification);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                throw new LdapException("Error while resetting user password in LDAP: " + ldapResult.getResultCode());
            }
            connection.close();
            LdapUtil.release(connection);
            user = authenticate(user.getName(), newPassword, config);
            LdapUtil.removeConnection(user.getName(), config);
            return user;
        } catch (LDAPException e) {
            if (e.getResultCode().equals(ResultCode.UNWILLING_TO_PERFORM)){
                throw new LdapException("The LDAP backend is unwilling to reset the password. Please make sure that the password meets the minimum complexity requirements. Contact your LDAP administrator if you are unsure.");
            }
            throw new LdapException(e);
        } catch (LdapException e){
            if(e.getCause() != null && e.getCause().getMessage()!=null && e.getCause().getMessage().contains("data 533")){
                throw new LdapException("Password reset request was successfully sent, but the account is still locked out. Please contact your LDAP administrator.");
            }
            throw e;
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public boolean deleteUser(LdapUser user, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
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
                filter = Filter.createEqualityFilter(config.getSchema().ATTR_ENTRYUUID, LdapUtil.UUIDStringToByteArray(UUID));
            } else {
                filter = Filter.createEqualityFilter(config.getSchema().ATTR_ENTRYUUID, UUID);
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
            Filter filter = Filter.createEqualityFilter(config.getSchema().ATTR_MAIL, mail);
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
        user.setName(entry.getAttributeValue(config.getSchema().ATTR_CN));
        user.setFirstName(entry.getAttributeValue(config.getSchema().ATTR_FIRST_NAME));
        user.setLastName(entry.getAttributeValue(config.getSchema().ATTR_LAST_NAME));
        user.setUid(entry.getAttributeValue(config.getSchema().ATTR_UID));
        user.setMail(entry.getAttributeValue(config.getSchema().ATTR_MAIL));
        user.setPhone(entry.getAttributeValue(config.getSchema().ATTR_TELEPHONE_NUMBER));
        if (config.getType().equals(LdapType.ActiveDirectory)) {
          //for some reason the objectGUID is stored in binary format in Active Directory
            // Microsoft stores GUIDs in a binary format that differs from the RFC standard of UUIDs (RFC #4122).
            String uuid = LdapUtil.bytesToUUID(entry.getAttributeValueBytes(config.getSchema().ATTR_ENTRYUUID));
            user.setUUID(uuid);
        } else {
            user.setUUID(entry.getAttributeValue(config.getSchema().ATTR_ENTRYUUID));
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
            Filter userFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_USER);
            Filter searchFilter = Filter.createANDFilter(userFilter, filter);
            SearchResult searchResults = connection.search(config.getBaseDN(), SearchScope.SUB, searchFilter, config.getSchema().ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    String dn = entry.getAttributeValue(config.getSchema().ATTR_DN);
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

    private byte[] getActiveDirectoryPassword(String password) {
        // http://msdn.microsoft.com/en-us/library/cc223248.aspx
        try {
            return ('"'+password+'"').getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        }
        return password.getBytes();
    }
}
