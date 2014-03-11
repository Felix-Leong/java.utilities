/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth;

import de.ebf.utils.auth.ldap.LdapConfig;
import java.util.List;

/**
 *
 * @author Dominik
 */
public interface GroupManager<Group, User> {

   public Group createGroup(String groupName, LdapConfig config) throws AuthException;

   public Group getGroup(String UUID, LdapConfig config) throws AuthException;

   public Group updateGroup(Group group, LdapConfig oldConfig, LdapConfig newConfig) throws AuthException;

   public List<Group> getAllGroups(LdapConfig config) throws AuthException;

   public Boolean deleteGroup(String UUID, LdapConfig config) throws AuthException;

   public Group addUserToGroup(User user, Group group, LdapConfig config) throws AuthException;

   public Group removeUserFromGroup(User user, Group group, LdapConfig config) throws AuthException;
}
