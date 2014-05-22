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
public abstract class LDAPWriteTest {
    
    protected LdapConfig config;
    
    private static final String TEST_USER_NAME  = "junitTestUser";
    private static final String TEST_GROUP_NAME = "junitTestGroup";

    @Autowired
    LdapUserManager userManager;
    
    @Autowired
    LdapGroupManager groupManager;
    

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
        LdapUser user = new LdapUser();
        user.setName(TEST_USER_NAME);
        user.setDN("cn=junitTestUser,"+config.getBaseDN());
        user.setFirstName("first");
        user.setLastName("last");
        user.setMail("bla@blub.de");
        user.setPassword("c0mPl3x!");
        user.setDN("cn="+TEST_USER_NAME+","+config.getBaseDN());
        user = userManager.createUser(user, config);
        assert(user.getUUID()!=null);
    }
    
    @Test
    public void updateUser() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        user.setName("junitTestUser2");
        user.setMail("bla@blub.de");
        user = userManager.updateUser(user, config);
        assert(user.getName().equals("junitTestUser2"));
        assert(user.getMail().equals("bla@blub.de"));
        user.setName(TEST_USER_NAME);
        userManager.updateUser(user, config);
        assert(user.getName().equals(TEST_USER_NAME));
    }
    
    @Test
    public void resetUserPassword1() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        user = userManager.resetPassword(user, "c0mPl3x!!", config);
        userManager.authenticate(user.getName(), "c0mPl3x!!", config);
    }
    
    @Test
    public void resetUserPassword2() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        user.setPassword("c0mPl3x!!!");
        userManager.updateUser(user, config);
    }
    
    //TODO: add tests for non complex passwords
    
    @Test
    public void addGroup() throws LdapException{
        LdapGroup group = new LdapGroup();
        group.setName(TEST_GROUP_NAME);
        group = groupManager.createGroup(group, config);
        assert(group.getUUID()!=null);
    }
    
    @Test
    public void updateGroup() throws LdapException{
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        group.setName("junitTestGroup2");
        group = groupManager.updateGroup(group, config);
        assert(group.getName().equals("junitTestGroup2"));
        group.setName(TEST_GROUP_NAME);
        group = groupManager.updateGroup(group, config);
        assert(group.getName().equals(TEST_GROUP_NAME));
    }
    
    @Test
    public void addUserToGoup() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        group = groupManager.addUserToGroup(user, group, config);
        assert(group.getMembers().contains(user));
    }
    
    @Test 
    public void removeUserFromGroup() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        group = groupManager.removeUserFromGroup(user, group, config);
        assert(!group.getMembers().contains(user));
    }
    
    @Test
    public void deleteGroup() throws LdapException{
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        groupManager.deleteGroup(group, config);
        assert(groupManager.getGroupByUUID(group.getUUID(), config) == null);
    }

    @Test
    public void deleteUser() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        userManager.deleteUser(user, config);
        assert(userManager.getUserByUUID(user.getUUID(), config)==null);
    }
}
