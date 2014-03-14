/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.ldap.config.LdapConfig;
import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ModifyDNRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import de.ebf.utils.auth.OrganizationManager;
import org.springframework.stereotype.Component;

/**
 *
 * @author Dominik
 */
@Component
public class LdapOrganizationManager implements OrganizationManager {
    
    public String checkIfBaseDNIsValid(LdapConfig config) throws LdapException{
        LDAPConnection connection = null;
        try {
            Filter userFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_USER);
            Filter groupFilter = Filter.createEqualityFilter(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECTCLASS_GROUP);
            Filter searchFilter = Filter.createORFilter(userFilter, groupFilter);
            connection = LdapUtil.getConnection(config);
            SearchResult searchResult = connection.search(config.getBaseDN(), SearchScope.SUB, searchFilter, config.getSchema().ATTR_ALL);
            if (searchResult!=null && searchResult.getEntryCount()>0){
                return config.getBaseDN();
            } else {
                throw new LdapException("Supplied entry does not contain any users or groups");
            }
        } catch (LDAPException ex) {
            throw new LdapException(ex);
        } finally {
            LdapUtil.release(connection);
        }
    }

    @Override
    public String addOrganization(String name, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            DN dn = getDN(name, config);
            Entry entry = new Entry(dn);
            entry.addAttribute(config.getSchema().ATTR_OBJECTCLASS, config.getSchema().OBJECT_CLASS_OU);
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
    public String updateOrganization(String oldDNStr, String newName, LdapConfig config) throws LdapException {
        LDAPConnection connection = null;
        try {
            DN oldDN = new DN(oldDNStr);
            DN newDN = getDN(newName, config);
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

   private DN getDN(String name, LdapConfig config) throws LDAPException {
       return new DN("ou=" + name + "," + config.getBaseDN());
   }
}
