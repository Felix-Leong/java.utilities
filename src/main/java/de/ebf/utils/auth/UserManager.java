/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import java.util.List;

/**
 *
 * @author dwissk
 */
public interface UserManager<User> {

   public User createUser(String username, String context) throws AuthException;

   public User updateUser(User user, String oldContext, String newContext) throws AuthException;

   public User authenticate(String username, String password, String context) throws AuthException;

   public User getUser(String username, String context) throws AuthException;

   public User resetPassword(String username, String newPassword, String context) throws AuthException;

   public boolean deleteUser(String id, String context) throws AuthException;

   public List<User> getAllUsers(String context) throws AuthException;
}
