/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapGroupManagerI;
import de.ebf.utils.auth.ldap.LdapUserManagerI;
import de.ebf.utils.auth.ldap.LdapUtil;
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
public abstract class LDAPReadTest {
    
    protected LdapConfig config;
    
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
   public void test01_connect() throws Exception{
       LdapUtil.verifyConnection(config);
   }
}
