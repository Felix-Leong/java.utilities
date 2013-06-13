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
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author Dominik
 */
@Component
public class LdapGroupManager implements GroupManager<LdapGroup, LdapUser> {
   
   @Override
   public LdapGroup createGroup(String groupName) throws LdapException {
      try {
         Entry entry = new Entry(LdapUtil.getDN(groupName));
         entry.addAttribute(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.ATTR_OBJECTCLASS_GROUP);
         entry.addAttribute(LdapUtil.ATTR_CN, groupName);
         AddRequest addRequest = new AddRequest(entry);
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         LDAPResult ldapResult = connection.add(addRequest);
         if (ldapResult.getResultCode() == (ResultCode.SUCCESS)) {
            return getGroup(groupName);
         } else {
            throw new LdapException("Adding group returned LDAP result code " + ldapResult.getResultCode());
         }
      } catch (LDAPException e) {
         throw new LdapException(e);
      }
   }

   @Override
   public LdapGroup getGroup(String groupName) throws LdapException {
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         return getGroupByAttribute(connection, LdapUtil.ATTR_CN, groupName);
      } catch (LDAPException ex) {
         throw new LdapException(ex);
      }
   }
   
   public LdapGroup getGroupByUUID(String UUID) throws LdapException {
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         return getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, UUID);
      } catch (LDAPException ex) {
         throw new LdapException(ex);
      }
   }

   @Override
   public LdapGroup updateGroup(LdapGroup group) throws LdapException {
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         LdapGroup currentGroup = getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, group.getUUID());

         if (!StringUtils.isEmpty(group.getName())) {
            if (!currentGroup.getName().equals(group.getName())) {
               ModifyDNRequest modifyDNRequest = new ModifyDNRequest(currentGroup.getDN(), "cn=" + group.getName(), true);
               LDAPResult ldapResult = connection.modifyDN(modifyDNRequest);
               if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                  throw new LdapException("Renaming group returned LDAP result code " + ldapResult.getResultCode());
               }
            }
         }
         return getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, group.getUUID());
      } catch (LDAPException e) {
         throw new LdapException(e);
      }
   }

   @Override
   public List<LdapGroup> getAllGroups() throws LdapException{
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         Filter groupFilter = Filter.create("objectClass="+LdapUtil.ATTR_OBJECTCLASS_GROUP);
         SearchResult searchResults = connection.search(LdapConfig.getContext(), SearchScope.SUB, groupFilter, LdapUtil.ATTR_ALL);
         if (searchResults.getEntryCount() > 0) {
            List<LdapGroup> groups = new ArrayList<>();
            for (SearchResultEntry entry : searchResults.getSearchEntries()) {
               groups.add(getLdapGroup(connection, entry));
            }
            return groups;
         } else {
            throw new LdapException("Could not find an entry that matches given criteria.");
         }
      } catch (LDAPException e) {
         throw new LdapException(e);
      }
   }

   @Override
   public Boolean deleteGroup(String UUID) throws LdapException {
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         LdapGroup group = getGroupByAttribute(connection, LdapUtil.ATTR_ENTRYUUID, UUID);
         DeleteRequest deleteRequest = new DeleteRequest(group.getDN());
         LDAPResult ldapResult = connection.delete(deleteRequest);
         return (ldapResult.getResultCode() == ResultCode.SUCCESS);
      } catch (LDAPException e) {
         throw new LdapException(e);
      }
   }
   
   private LdapGroup getGroupByAttribute(LDAPConnection connection, String attribute, String value) throws LdapException {
      try {
         SearchResult searchResults = connection.search(LdapConfig.getContext(), SearchScope.SUB, "(" + attribute + "=" + value + ")", LdapUtil.ATTR_ALL);
         if (searchResults.getEntryCount() == 1) {
            return getLdapGroup(connection, searchResults.getSearchEntries().get(0));
         } else {
            throw new LdapException("Unexpected number of LDAP search results: " + searchResults.getEntryCount());
         }
      } catch (LDAPException e) {
         throw new LdapException(e);
      }
   }
   
   private LdapGroup getLdapGroup(LDAPConnection connection, SearchResultEntry entry) throws LdapException{
      LdapGroup group = new LdapGroup();
      group.setName(entry.getAttributeValue(LdapUtil.ATTR_CN));
      group.setUUID(entry.getAttributeValue(LdapUtil.ATTR_ENTRYUUID));
      group.setDN(entry.getDN());
      String members = entry.getAttributeValue(LdapUtil.ATTR_MEMBERS);
      Collection<Attribute> attributes = entry.getAttributes();
      Attribute attr = entry.getAttribute(LdapUtil.ATTR_MEMBERS);
      if (attr!=null){
         String[] userDNs = attr.getValues();
         List<LdapUser> users = new ArrayList<>(userDNs.length);
         for (int i=0; i<userDNs.length; i++)
         {
            String cn = LdapUtil.getCN(userDNs[i]);
            users.add(LdapUserManager.getUserByAttribute(connection, "cn", cn));
         }
         group.setMembers(users);     
      }
      //TODO        
      return group;
   }

   @Override
   public LdapGroup removeUserFromGroup(LdapUser user, LdapGroup group) throws AuthException {
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         Modification modification = new Modification(ModificationType.DELETE, LdapUtil.ATTR_MEMBERS, user.getDN());
         ModifyRequest modifyRequest = new ModifyRequest(group.getDN(), modification);
         LDAPResult ldapResult = connection.modify(modifyRequest);
         if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
            throw new LdapException("Removing user from group returned LDAP result code " + ldapResult.getResultCode());
         }
         return getGroup(group.getName());
      } catch (LDAPException e){
         throw new LdapException(e);
      }  
   }

   @Override
   public LdapGroup addUserToGroup(LdapUser user, LdapGroup group) throws AuthException {
      try {
         LDAPConnection connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass());
         Modification modification = new Modification(ModificationType.ADD, LdapUtil.ATTR_MEMBERS, user.getDN());
         ModifyRequest modifyRequest = new ModifyRequest(group.getDN(), modification);
         LDAPResult ldapResult = connection.modify(modifyRequest);
         if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
            throw new LdapException("Adding user to group returned LDAP result code " + ldapResult.getResultCode());
         }
         return getGroup(group.getName());
      } catch (LDAPException e){
         throw new LdapException(e);
      } 
   }
}
