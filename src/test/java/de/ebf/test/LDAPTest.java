/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.LdapGroupManager;
import de.ebf.utils.auth.ldap.LdapUser;
import de.ebf.utils.auth.ldap.LdapUserManager;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
    protected static LdapConfig config;

    @Autowired
    LdapUserManager userManager;
    
    @Autowired
    LdapGroupManager groupManager;

    
    public LDAPTest() {
        // override in subclass
    }
    
    protected static void initUser() {
        user = new LdapUser();
        user.setName("junitTestUser");
        user.setDN("cn=junitTestUser,"+config.getBaseDN());
        user.setFirstName("first");
        user.setLastName("last");
        user.setMail("bla@blub.de");
        user.setPassword("c0mPl3x!");
        user.setDN("cn=junitTestUser,"+config.getBaseDN());
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
    }
    
    @Test
    public void updateUser() throws LdapException{
        user.setName("junitTestUser2");
        user = userManager.updateUser(user, config);
    }

    @Test
    public void deleteUser() throws LdapException{
        userManager.deleteUser(user, config);
    }
}
