/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapType;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.junit.BeforeClass;

/**
 *
 * @author dominik
 */
public class ActiveDirectoryTest extends LDAPTest{
    
    @BeforeClass
    public static void setupConfig() {
        config = new LdapConfig();
        config.setServer("10.4.6.12");
        config.setPort(636);
        config.setBaseDN("dc=tba,dc=ebf,dc=de");
        config.setUsername("cn=Administrator,cn=Users,dc=tba,dc=ebf,dc=de");
        config.setPassword("!telek0m2014");
        config.setType(LdapType.ActiveDirectory);
        initUser();
    }
}
