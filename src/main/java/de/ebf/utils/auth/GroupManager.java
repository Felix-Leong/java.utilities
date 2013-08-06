/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import java.util.List;

/**
 *
 * @author Dominik
 */
public interface GroupManager<Group, User> {

   public Group createGroup(String groupName, String context) throws AuthException;

   public Group getGroup(String UUID, String context) throws AuthException;

   public Group updateGroup(Group group, String oldContext, String newContext) throws AuthException;

   public List<Group> getAllGroups(String context) throws AuthException;

   public Boolean deleteGroup(String UUID, String context) throws AuthException;

   public Group addUserToGroup(User user, Group group, String context) throws AuthException;

   public Group removeUserFromGroup(User user, Group group, String context) throws AuthException;
}
