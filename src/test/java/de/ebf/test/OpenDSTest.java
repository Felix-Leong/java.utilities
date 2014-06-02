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
public class OpenDSTest extends LDAPWriteTest{
    
    //@BeforeClass
    public OpenDSTest(){
        config = new LdapConfig();
        config.setServer("127.0.0.1");
        config.setPort(1389);
        config.setBaseDN("dc=tba,dc=ebf,dc=de");
        config.setUsername("cn=Directory Manager");
        config.setPassword("secret");
        config.setType(LdapType.OpenDS);
    }
}
