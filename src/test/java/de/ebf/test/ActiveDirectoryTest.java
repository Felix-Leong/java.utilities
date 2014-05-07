/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapType;
import de.ebf.utils.auth.ldap.config.LdapConfig;

/**
 *
 * @author dominik
 */
public class ActiveDirectoryTest extends LDAPTest{
    
    public ActiveDirectoryTest() {
        config = new LdapConfig();
        config.setServer("10.4.6.12");
        config.setPort(636);
        config.setBaseDN("dc=tba,dc=ebf,dc=de");
        config.setUsername("cn=Administrator,cn=Users,dc=tba,dc=ebf,dc=de");
        config.setPassword("!Telek0m");
        config.setType(LdapType.ActiveDirectory);
    }
}
