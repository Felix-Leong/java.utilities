/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.LdapGroup;
import de.ebf.utils.auth.ldap.LdapGroupManager;
import de.ebf.utils.auth.ldap.LdapUser;
import de.ebf.utils.auth.ldap.LdapUserManager;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author dominik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
public abstract class LDAPTest {
    
    private static LdapUser user;
    private static LdapGroup group;
    protected static LdapConfig config;

    @Autowired
    LdapUserManager userManager;
    
    @Autowired
    LdapGroupManager groupManager;
    
    /*
    TODO: 
        add tests for non complex passwords
    */

    protected static void initData() {
        user = new LdapUser();
        user.setName("junitTestUser");
        user.setDN("cn=junitTestUser,"+config.getBaseDN());
        user.setFirstName("first");
        user.setLastName("last");
        user.setMail("bla@blub.de");
        user.setPassword("c0mPl3x!");
        user.setDN("cn=junitTestUser,"+config.getBaseDN());
        
        group = new LdapGroup();
        group.setName("junitTestGroup");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void addUser() throws LdapException{
        user = userManager.createUser(user, config);
        assert(user.getUUID()!=null);
    }
    
    @Test
    public void updateUser() throws LdapException{
        user.setName("junitTestUser2");
        user.setMail("bla@blub.de");
        user = userManager.updateUser(user, config);
        assert(user.getName().equals("junitTestUser2"));
        assert(user.getMail().equals("bla@blub.de"));
    }
    
    @Test
    public void resetUserPassword1() throws LdapException{
        user = userManager.resetPassword(user, "c0mPl3x!!", config);
    }
    
    @Test
    public void authenticateUser1() throws LdapException{
        user = userManager.authenticate(user.getName(), "c0mPl3x!!", config);
    }
    
    @Test
    public void resetUserPassword2() throws LdapException{
        user.setPassword("c0mPl3x!!!");
        user = userManager.updateUser(user, config);
    }
    
    @Test
    public void authenticateUser2() throws LdapException{
        user = userManager.authenticate(user.getName(), "c0mPl3x!!!", config);
    }
    
    @Test
    public void addGroup() throws LdapException{
        group = groupManager.createGroup(group, config);
        assert(group.getUUID()!=null);
    }
    
    @Test
    public void updateGroup() throws LdapException{
        assert(group.getName().equals("junitTestGroup"));
        group.setName("junitTestGroup2");
        group = groupManager.updateGroup(group, config);
        assert(group.getName().equals("junitTestGroup2"));
    }
    
    @Test
    public void addUserToGoup() throws LdapException{
        group = groupManager.addUserToGroup(user, group, config);
        assert(group.getMembers().contains(user));
    }
    
    @Test 
    public void removeUserFromGroup() throws LdapException{
        group = groupManager.removeUserFromGroup(user, group, config);
        assert(!group.getMembers().contains(user));
    }
    
    @Test
    public void deleteGroup() throws LdapException{
        groupManager.deleteGroup(group, config);
        assert(groupManager.getGroupByUUID(group.getUUID(), config) == null);
    }

    @Test
    public void deleteUser() throws LdapException{
        userManager.deleteUser(user, config);
        assert(userManager.getUserByUUID(user.getUUID(), config)==null);
    }
}
