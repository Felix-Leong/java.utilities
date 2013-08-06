/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.LdapOrganization;
import java.util.List;

/**
 *
 * @author Dominik
 */
public interface OrganizationManager {

   public String addOrganization(String name) throws LdapException;

   public String renameOrganization(String DN, String name) throws LdapException;

   public Boolean deleteOrganization(String DN) throws LdapException;

   public List<LdapOrganization> getLdapOrganizations(String baseDN) throws LdapException;
}
