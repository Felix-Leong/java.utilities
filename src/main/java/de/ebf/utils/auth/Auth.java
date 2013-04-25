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

   public User createOrUpdateUser(String username, String password) throws RuntimeException;

   public User authenticate(String username, String password) throws IllegalArgumentException;

   public User getUser(String username) throws IllegalArgumentException;

   public User resetPassword(String username, String newPassword) throws IllegalArgumentException;

   public boolean deleteUser(String username) throws IllegalArgumentException;

   public List<User> getAllUsers();
}
