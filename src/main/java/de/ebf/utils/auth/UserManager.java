/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import de.ebf.utils.auth.ldap.config.LdapConfig;
import java.util.List;

/**
 *
 * @author dwissk
 */
public interface UserManager<User> {

   public User createUser(String username, String firstname, String lastname, LdapConfig config) throws AuthException;

   public User updateUser(User user, LdapConfig oldConfig, LdapConfig newConfig) throws AuthException;

   public User authenticate(String username, String password, LdapConfig config) throws AuthException;

   public User getUser(String username, LdapConfig config) throws AuthException;

   public User resetPassword(User user, String newPassword, LdapConfig config) throws AuthException;

   public boolean deleteUser(String id, LdapConfig config) throws AuthException;

   public List<User> getAllUsers(LdapConfig config) throws AuthException;
}
