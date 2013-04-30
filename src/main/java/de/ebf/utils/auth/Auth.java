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
public interface Auth<User> {

   public User createUser(String username) throws AuthException;

   public User updateUser(User user) throws AuthException;

   public User authenticate(String username, String password) throws AuthException;

   public User getUser(String username) throws AuthException;

   public User resetPassword(String username, String newPassword) throws AuthException;

   public boolean deleteUser(String id) throws AuthException;

   public List<User> getAllUsers() throws AuthException;
}
