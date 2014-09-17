/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.auth.ldap;

import de.ebf.utils.auth.GroupManager;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import java.util.List;

/**
 *
 * @author dominik
 */
public interface LdapGroupManagerI extends GroupManager<LdapGroup, LdapUser> {

    @Override
    public LdapGroup createGroup(LdapGroup group, LdapConfig config) throws LdapException;

    @Override
    public LdapGroup getGroup(String UUID, LdapConfig config) throws LdapException;

    @Override
    public LdapGroup updateGroup(LdapGroup group, LdapConfig config) throws LdapException;

    @Override
    public List<LdapGroup> getAllGroups(LdapConfig config) throws LdapException;

    @Override
    public Boolean deleteGroup(LdapGroup group, LdapConfig config) throws LdapException;

    @Override
    public LdapGroup addUserToGroup(LdapUser user, LdapGroup group, LdapConfig config) throws LdapException;

    @Override
    public LdapGroup removeUserFromGroup(LdapUser user, LdapGroup group, LdapConfig config) throws LdapException;

    public List<LdapGroup> getGroupsByApproximateMatch(String groupName, LdapConfig config) throws LdapException;
    
    LdapGroup getGroupByUUID(String UUID, LdapConfig config) throws LdapException;

    List<LdapGroup> getGroupsForUser(LdapUser user, LdapConfig config) throws LdapException;
}
