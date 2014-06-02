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
import de.ebf.utils.auth.ldap.LdapUtil;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.apache.commons.lang.StringUtils;
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
public abstract class LDAPReadTest {
    
    protected LdapConfig config;
    
    private static final String TEST_USER_NAME  = "Administrator";
    private static final String TEST_GROUP_NAME = "Administrators";

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
   public void connect() throws Exception{
       LdapUtil.verifyConnection(config);
   }
   
   @Test
   public void getUser() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        assert(user!=null);
   }
   
   @Test
   public void getGroup() throws LdapException{
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        assert(group!=null);
   }
   
   @Test
   public void getMember() throws LdapException{
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        assert(group.getMembers()!=null && group.getMembers().size()>0);
        assert(group.getMembers().contains(user));
   }
}
