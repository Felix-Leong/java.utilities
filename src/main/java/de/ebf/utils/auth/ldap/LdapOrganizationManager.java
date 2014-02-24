/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ModifyDNRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import de.ebf.utils.auth.OrganizationManager;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Dominik
 */
@Component
public class LdapOrganizationManager implements OrganizationManager {

    @Override
    public String addOrganization(String name) throws LdapException {
        LDAPConnection connection = null;
        try {
            DN dn = getDN(name);
            Entry entry = new Entry(dn);
            entry.addAttribute(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECT_CLASS_ORGANIZATION);
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), LdapConfig.getContext());
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                throw new LdapException("Adding Organization to LDAP returned result code " + ldapResult.getResultCode());
            }
            return dn.toNormalizedString();
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public String renameOrganization(String DN, String name) throws LdapException {
        LDAPConnection connection = null;
        try {
            DN oldDN = new DN(DN);
            DN newDN = getDN(name);
            ModifyDNRequest request = new ModifyDNRequest(oldDN, newDN.getRDN(), true);
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), LdapConfig.getContext());
            LDAPResult ldapResult = connection.modifyDN(request);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                throw new LdapException("Renaming Organization int LDAP returned result code " + ldapResult.getResultCode());
            }
            return newDN.toNormalizedString();
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

    @Override
    public Boolean deleteOrganization(String DN) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), LdapConfig.getContext());
            DeleteRequest request = new DeleteRequest(DN);
            LDAPResult ldapResult = connection.delete(request);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                throw new LdapException("Deleting Organization from LDAP returned result code " + ldapResult.getResultCode());
            }
            return true;
        } catch (LDAPException | LdapException e) {
            throw new LdapException(e);
        } finally {
            if (connection != null) {
                LdapUtil.release(connection);
            }
        }
    }

   @Override
   public List<LdapOrganization> getLdapOrganizations(String baseDN) throws LdapException {
      LDAPConnection connection = null;
      try {
         connection = LdapUtil.getConnection(LdapConfig.getUser(), LdapConfig.getPass(), LdapConfig.getContext());
         List<LdapOrganization> OUs = new ArrayList<>();
         SearchResult searchResults = connection.search(LdapConfig.getContext(), SearchScope.ONE, (LdapUtil.ATTR_OBJECTCLASS + "=" + LdapUtil.OBJECT_CLASS_ORGANIZATION), LdapUtil.ATTR_DN);
         for (SearchResultEntry entry : searchResults.getSearchEntries()) {
            LdapOrganization OU = new LdapOrganization();
            OU.setDN(entry.getDN());
            OUs.add(OU);
         }
         return OUs;
      } catch (Exception e) {
         throw new LdapException(e);
      } finally {
         LdapUtil.release(connection);
      }
   }

   private DN getDN(String name) throws LDAPException {
      return new DN("ou=" + name + "," + LdapConfig.getContext());
   }
}
