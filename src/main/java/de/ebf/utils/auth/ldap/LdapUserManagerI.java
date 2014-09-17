/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchResultEntry;
import de.ebf.utils.auth.UserManager;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import java.util.List;

/**
 *
 * @author dominik
 */
public interface LdapUserManagerI extends UserManager<LdapUser> {

    @Override
    public LdapUser getUser(String username, LdapConfig config) throws LdapException;

    @Override
    public LdapUser createUser(LdapUser user, LdapConfig config) throws LdapException;

    @Override
    public LdapUser updateUser(LdapUser user, LdapConfig newConfig) throws LdapException;

    @Override
    public LdapUser authenticate(String username, String password, LdapConfig config) throws LdapException;

    @Override
    public LdapUser resetPassword(LdapUser user, String newPassword, LdapConfig config) throws LdapException;

    @Override
    public boolean deleteUser(LdapUser user, LdapConfig config) throws LdapException;

    @Override
    public List<LdapUser> getAllUsers(LdapConfig config) throws LdapException;

    String getAttribute(LdapUser user, String attributeName, LdapConfig config) throws LdapException;

    LdapUser getLdapUser(SearchResultEntry entry, LdapConfig config) throws LdapException;

    LdapUser getUserByFilter(LDAPConnection connection, Filter filter, LdapConfig config) throws LdapException;

    LdapUser getUserByUUID(String UUID, LdapConfig config) throws LdapException;

    List<LdapUser> getUsersByFilter(LDAPConnection connection, Filter filter, LdapConfig config) throws LdapException;

    List<LdapUser> getUsersByMail(String mail, LdapConfig config) throws LdapException;

}
