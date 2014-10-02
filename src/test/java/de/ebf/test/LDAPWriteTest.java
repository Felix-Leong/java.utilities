/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.LdapGroup;
import de.ebf.utils.auth.ldap.LdapGroupManagerI;
import de.ebf.utils.auth.ldap.LdapUser;
import de.ebf.utils.auth.ldap.LdapUserManagerI;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author dominik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class LDAPWriteTest {
    
    protected LdapConfig config;
    
    private static final String TEST_USER_NAME  = "junitTestUser";
    private static final String TEST_GROUP_NAME = "junitTestGroup";

    @Autowired
    LdapUserManagerI ldapUserManager;
    
    @Autowired
    LdapGroupManagerI ldapGroupManager;
    

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
    public void test01_addUser() throws LdapException{
        LdapUser user = new LdapUser();
        user.setName(TEST_USER_NAME);
        user.setDN("cn=junitTestUser,"+config.getBaseDN());
        user.setFirstName("first");
        user.setLastName("last");
        user.setMail("bla@blub.de");
        user.setPassword("c0mPl3x!");
        user.setDN("cn="+TEST_USER_NAME+","+config.getBaseDN());
        user = ldapUserManager.createUser(user, config);
        assert(user.getUUID()!=null);
    }
    
    @Test
    public void test02_resetUserPassword1() throws LdapException{
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        user = ldapUserManager.resetPassword(user, "c0mPl3x!!", config);
        ldapUserManager.authenticate(user.getName(), "c0mPl3x!!", config);
    }
    
    @Test
    public void test03_resetUserPassword2() throws LdapException{
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        user.setPassword("c0mPl3x!!!");
        ldapUserManager.updateUser(user, config);
    }
    
    @Test
    public void test04_updateUser() throws LdapException{
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        user.setName("junitTestUser2");
        user.setMail("bla@blub.de");
        user = ldapUserManager.updateUser(user, config);
        assert(user.getName().equals("junitTestUser2"));
        assert(user.getMail().equals("bla@blub.de"));
        user.setName(TEST_USER_NAME);
        ldapUserManager.updateUser(user, config);
        assert(user.getName().equals(TEST_USER_NAME));
    }
    
    @Test
    public void test05_addGroup() throws LdapException{
        LdapGroup group = new LdapGroup();
        group.setName(TEST_GROUP_NAME);
        group = ldapGroupManager.createGroup(group, config);
        assert(group.getUUID()!=null);
    }
    
    @Test
    public void test06_updateGroup() throws LdapException{
        LdapGroup group = ldapGroupManager.getGroup(TEST_GROUP_NAME, false, config);
        group.setName("junitTestGroup2");
        group = ldapGroupManager.updateGroup(group, config);
        assert(group.getName().equals("junitTestGroup2"));
        group.setName(TEST_GROUP_NAME);
        group = ldapGroupManager.updateGroup(group, config);
        assert(group.getName().equals(TEST_GROUP_NAME));
    }
    
    @Test
    public void test07_addUserToGoup() throws LdapException{
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = ldapGroupManager.getGroup(TEST_GROUP_NAME, true, config);
        group = ldapGroupManager.addUserToGroup(user, group, config);
        assert(group.getMembers().contains(user));
    }
    
    @Test 
    public void test08_removeUserFromGroup() throws LdapException{
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = ldapGroupManager.getGroup(TEST_GROUP_NAME, true, config);
        group = ldapGroupManager.removeUserFromGroup(user, group, config);
        assert(!group.getMembers().contains(user));
    }
    
    @Test
    public void test09_deleteGroup() throws LdapException{
        LdapGroup group = ldapGroupManager.getGroup(TEST_GROUP_NAME, false, config);
        ldapGroupManager.deleteGroup(group, config);
        assert(ldapGroupManager.getGroupByUUID(group.getUUID(), false, config) == null);
    }

    @Test
    public void test10_deleteUser() throws LdapException{
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        ldapUserManager.deleteUser(user, config);
        assert(ldapUserManager.getUserByUUID(user.getUUID(), config)==null);
    }
}
