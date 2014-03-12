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
    public String addOrganization(String name, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            DN dn = getDN(name, config);
            Entry entry = new Entry(dn);
            entry.addAttribute(LdapUtil.ATTR_OBJECTCLASS, LdapUtil.OBJECT_CLASS_OU);
            AddRequest addRequest = new AddRequest(entry);
            connection = LdapUtil.getConnection(config);
            LDAPResult ldapResult = connection.add(addRequest);
            if (ldapResult.getResultCode() != (ResultCode.SUCCESS)) {
                throw new LdapException("Adding Organization to LDAP returned result code " + ldapResult.getResultCode());
            }
            return dn.toNormalizedString();
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public String updateOrganization(String DN, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            DN oldDN = new DN(DN);
            DN newDN = new DN(config.getBaseDN());
            if (!oldDN.getRDNString().equals(newDN.getRDNString())){
                ModifyDNRequest request = new ModifyDNRequest(oldDN, newDN.getRDN(), true);
                connection = LdapUtil.getConnection(config);
                LDAPResult ldapResult = connection.modifyDN(request);
                if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                    throw new LdapException("Renaming Organization int LDAP returned result code " + ldapResult.getResultCode());
                }
            }
            return newDN.toNormalizedString();
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public Boolean deleteOrganization(String DN, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            connection = LdapUtil.getConnection(config);
            DeleteRequest request = new DeleteRequest(DN);
            LDAPResult ldapResult = connection.delete(request);
            if (ldapResult.getResultCode() != ResultCode.SUCCESS) {
                throw new LdapException("Deleting Organization from LDAP returned result code " + ldapResult.getResultCode());
            }
            return true;
        } catch (LDAPException | LdapException e) {
            throw new LdapException(e);
        } finally {
            LdapUtil.release(connection);
        }
    }

   @Override
   public List<LdapOrganization> getLdapOrganizations(String baseDN, LdapConfig config) throws LdapException {
      LDAPConnection connection = null;
      try {
         connection = LdapUtil.getConnection(config);
         List<LdapOrganization> OUs = new ArrayList<>();
         SearchResult searchResults = connection.search(config.getBaseDN(), SearchScope.ONE, (LdapUtil.ATTR_OBJECTCLASS + "=" + LdapUtil.OBJECT_CLASS_OU), LdapUtil.ATTR_DN);
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

   private DN getDN(String name, LdapConfig config) throws LDAPException {
       return new DN("ou=" + name + "," + config.getBaseDN());
   }
}
