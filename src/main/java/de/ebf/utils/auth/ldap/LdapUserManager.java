package de.ebf.utils.auth.ldap;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.TriggersRemove;
import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DN;
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
import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import de.ebf.cache.CacheName;
import de.ebf.utils.Bundle;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import de.ebf.utils.auth.ldap.schema.ActiveDirectorySchema;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class LdapUserManager implements LdapUserManagerI {
    
    private static final Logger log = Logger.getLogger(LdapUserManager.class);
    
    @Autowired
    private LdapGroupManagerI ldapGroupManager;
    
    @Override
    @TriggersRemove(cacheName=CacheName.getAllUsers, removeAll=true)
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
                entry.addAttribute(ActiveDirectorySchema.ATTR_USER_ACCOUNT_CONTROL, "544");
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
    @TriggersRemove(cacheName={CacheName.getUser, CacheName.getAllUsers}, removeAll=true)
    public LdapUser updateUser(LdapUser user, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            LdapUser oldUser = getUserByUUID(user.getUUID(), config);
            connection = LdapUtil.getConnection(config);
            List<Modification> mods = new ArrayList<>();
            
            DN oldDN = new DN(oldUser.getDN());
            DN newDN = new DN(user.getDN());
            
            if (oldDN.equals(newDN) && !oldUser.getName().equals(user.getName())){
                //renaming a user is the same as changing its DN
                newDN = new DN(new RDN(config.getSchema().ATTR_CN, user.getName()), newDN.getParent());
            }
                
            if (!oldDN.equals(newDN)) {

                //get all groups for current user before renaming user. Otherwise group.getMembers() will already contain renamed users
                List<LdapGroup> allGroups = ldapGroupManager.getGroupsForUser(oldUser, true, config);
                
                //update user DN
                ModifyDNRequest modifyDNRequest = new ModifyDNRequest(oldDN, newDN.getRDN(), true, newDN.getParent());
                LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
                if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                    throw new LdapException("Updating user DN returned LDAP result code " + ldapResult.getResultCode());
                }

                //List<LdapGroup> allGroups = ldapGroupManager.getAllGroups(oldContext);
                if (config.getType().equals(LdapType.OpenDS)){
                    //also update all dn membership values, since OpenDS doesn't take care of this
                    for (LdapGroup ldapGroup : allGroups) {
                        Modification deleteOldUserDN = new Modification(ModificationType.DELETE, config.getSchema().ATTR_MEMBERS, oldUser.getDN());
                        Modification addNewUserDN = new Modification(ModificationType.ADD, config.getSchema().ATTR_MEMBERS, user.getDN());
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
            
            //update first name
            if (!StringUtils.isEmpty(user.getFirstName())) {
                mods.add(new Modification(ModificationType.REPLACE, config.getSchema().ATTR_FIRST_NAME, user.getFirstName()));
            }

            //update last name
            if (!StringUtils.isEmpty(user.getLastName())) {
                mods.add(new Modification(ModificationType.REPLACE, config.getSchema().ATTR_LAST_NAME, user.getLastName()));
            }

            //update email
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
            
            //reset password
            if (!StringUtils.isEmpty(user.getPassword())) {
                resetPassword(user, user.getPassword(), config);
            }
            
            //removeUserFromCache(oldUser, config);
            
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
    @Cacheable(cacheName=CacheName.getUser, cacheNull=false)
    public LdapUser getUser(String userName, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config.getUsername(), config.getPassword(), config);
            Filter filter;
            Filter cnFilter = Filter.createEqualityFilter(config.getSchema().ATTR_CN, userName);
            Filter mailFilter = Filter.createEqualityFilter(config.getSchema().ATTR_MAIL, userName);
            if (config.getType().equals(LdapType.ActiveDirectory)){
                Filter samAccountNameFilter     = Filter.createEqualityFilter(ActiveDirectorySchema.ATTR_SAM_ACCOUNT_NAME, userName);
                Filter userPrincipalNameFilter  = Filter.createEqualityFilter(ActiveDirectorySchema.ATTR_USER_PRINCIPAL_NAME, userName);
                filter = Filter.createORFilter(cnFilter, mailFilter, samAccountNameFilter, userPrincipalNameFilter);
            } else {
                filter = Filter.createORFilter(cnFilter, mailFilter);
            }
            LdapUser user = getUserByFilter(connection, filter, config);
            return user;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    @Cacheable(cacheName=CacheName.getAllUsers)
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
    @TriggersRemove(cacheName={CacheName.getUser}, removeAll=true)
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
            
            //removeUserFromCache(user, config);
            
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
    @TriggersRemove(cacheName={CacheName.getUser, CacheName.getAllUsers}, removeAll=true)
    public boolean deleteUser(LdapUser user, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            List<LdapGroup> groups = ldapGroupManager.getGroupsForUser(user, true, config);
            for (LdapGroup group : groups) {
                ldapGroupManager.removeUserFromGroup(user, group, config);
            }
            DeleteRequest deleteRequest = new DeleteRequest(user.getDN());
            LDAPResult ldapResult = connection.delete(deleteRequest);
            return (ldapResult.getResultCode() == ResultCode.SUCCESS);
        } catch (LDAPException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public LdapUser getUserByUUID(String UUID, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            Filter filter = createUUIDFilter(UUID, config);
            connection = LdapUtil.getConnection(config);
            LdapUser user = getUserByFilter(connection, filter, config);
            return user;
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
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

    @Override
    public LdapUser getLdapUser(SearchResultEntry entry, LdapConfig config) throws LdapException {
        LdapUser user = new LdapUser();
        user.setDN(entry.getDN());
        user.setName(entry.getAttributeValue(config.getSchema().ATTR_CN));
        user.setFirstName(entry.getAttributeValue(config.getSchema().ATTR_FIRST_NAME));
        user.setLastName(entry.getAttributeValue(config.getSchema().ATTR_LAST_NAME));
        user.setMail(entry.getAttributeValue(config.getSchema().ATTR_MAIL));
        user.setPhone(entry.getAttributeValue(config.getSchema().ATTR_TELEPHONE_NUMBER));
        if (config.getType().equals(LdapType.ActiveDirectory)) {
            //for some reason the objectGUID is stored in binary format in Active Directory
            // Microsoft stores GUIDs in a binary format that differs from the RFC standard of UUIDs (RFC #4122).
            String uuid = LdapUtil.bytesToUUID(entry.getAttributeValueBytes(config.getSchema().ATTR_ENTRYUUID));
            user.setUUID(uuid);
            
            String sid = LdapUtil.bytesToSid(entry.getAttributeValueBytes(config.getSchema().ATTR_UID));
            user.setUid(sid);
            
            user.setPrimaryGroupId(entry.getAttributeValueAsInteger(ActiveDirectorySchema.ATTR_PRIMARY_GROUP_ID));
            
            //we cannot query AD by the primaryGroupId, only by objectSid. Therefore, we construct the primaryGroupObjectSid here
            //see also http://www.morgantechspace.com/2013/10/difference-between-rid-and-sid-in.html
            String uid = user.getUid();
            if (uid!=null && user.getPrimaryGroupId()!=null){
                String domainSid = uid.substring(0, uid.lastIndexOf("-"));
                user.setPrimaryGroupObjectSid(domainSid+"-"+user.getPrimaryGroupId());
            }
            user.setSAMAccountName(entry.getAttributeValue(ActiveDirectorySchema.ATTR_SAM_ACCOUNT_NAME));
            user.setUserPrincipalName(entry.getAttributeValue(ActiveDirectorySchema.ATTR_USER_PRINCIPAL_NAME));
            
            //see also LdapGroupManager.getGroupsForUser()
            String[] groupDNArray = entry.getAttributeValues(ActiveDirectorySchema.ATTR_MEMBER_OF);
            if (groupDNArray !=null){
                List<String> groupDNs = Arrays.asList(groupDNArray);
                user.setGroupDNs(groupDNs);
            }
        } else {
            //Domino and OpenDS store the GUID in clear text
            user.setUUID(entry.getAttributeValue(config.getSchema().ATTR_ENTRYUUID));
            user.setUid(entry.getAttributeValue(config.getSchema().ATTR_UID));
        }
        try {
            user.setContext(entry.getParentDNString());
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        }

        return user;
    }
    
    @Override
    public List<LdapUser> getUsersByFilter(LDAPConnection connection, Filter filter, LdapConfig config) throws LdapException {
        List<LdapUser> users = new ArrayList<>();
        try {
            Filter userFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_USER);
            Filter searchFilter;
            if (config.getType().equals(LdapType.ActiveDirectory)){
                //for some reason computer objects are also user objects in AD
                Filter notComputerFilter = Filter.createNOTFilter(Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, ActiveDirectorySchema.OBJECTCLASS_COMPUTER));
                Filter userButNotComputerFilter = Filter.createANDFilter(userFilter, notComputerFilter);
                searchFilter = Filter.createANDFilter(userButNotComputerFilter, filter);
            } else { 
                searchFilter = Filter.createANDFilter(userFilter, filter);
            }
            SearchResult searchResults = connection.search(config.getBaseDN(), SearchScope.SUB, searchFilter, config.getSchema().ATTR_ALL);
            if (searchResults.getEntryCount() > 0) {
                for (SearchResultEntry entry : searchResults.getSearchEntries()) {
                    users.add(getLdapUser(entry, config));
                }
                Collections.sort(users);
            }
        } catch (LDAPSearchException e) {
            throw new LdapException(e);
        }
        return users;
    }

    @Override
    public LdapUser getUserByFilter(LDAPConnection connection, Filter filter, LdapConfig config) throws LdapException {
        List<LdapUser> users = getUsersByFilter(connection, filter, config);
        if (users.size() == 1) {
            return users.get(0);
        } else if (users.size() > 1) {
            log.warn("Found multiple LDAP users for filter " + filter);
        }
        return null;
    }
    
    @Override
    public String getAttribute(LdapUser user, String attributeName, LdapConfig config) throws LdapException{
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            Filter userFilter = createUUIDFilter(user.getUUID(), config);
            SearchResult searchResults = connection.search(config.getBaseDN(), SearchScope.SUB, userFilter, attributeName);
            if (searchResults.getEntryCount() == 1) {
                return searchResults.getSearchEntries().get(0).getAttributeValue(attributeName);
            }
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
        return null;
    }

    @SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification = "zero length array would be misleading")
    private byte[] getActiveDirectoryPassword(String password) {
        // http://msdn.microsoft.com/en-us/library/cc223248.aspx
        try {
            return ('"'+password+'"').getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        }
        return null;
    }

    private Filter createUUIDFilter(String UUID, LdapConfig config) {
        Filter filter;
        if (config.getType().equals(LdapType.ActiveDirectory)) {
            filter = Filter.createEqualityFilter(config.getSchema().ATTR_ENTRYUUID, LdapUtil.UUIDStringToByteArray(UUID));
        } else {
            filter = Filter.createEqualityFilter(config.getSchema().ATTR_ENTRYUUID, UUID);
        }
        return filter;
    }
}
