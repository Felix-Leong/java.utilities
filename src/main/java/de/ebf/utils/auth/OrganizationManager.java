/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.config.LdapConfig;

/**
 *
 * @author Dominik
 */
public interface OrganizationManager {

   public String addOrganization(String name, LdapConfig config) throws LdapException;

   public String updateOrganization(String oldDN, String newName, LdapConfig config) throws LdapException;

   public Boolean deleteOrganization(String DN, LdapConfig config) throws LdapException;
}
