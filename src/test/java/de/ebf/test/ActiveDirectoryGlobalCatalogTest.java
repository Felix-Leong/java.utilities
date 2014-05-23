/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.LdapType;
import de.ebf.utils.auth.ldap.LdapUser;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.junit.Test;

/**
 *
 * @author dominik
 */
public class ActiveDirectoryGlobalCatalogTest extends LDAPReadTest{
    
    private static final String ACTIVE_DIRECTORY_SAM_ACCOUNT_NAME           = "sAMAccountName.Test";
    private static final String ACTIVE_DIRECTORY_USER_PRINCIPAL_NAME        = "sAMAccountName.Test@tba.ebf.de";
    
    public ActiveDirectoryGlobalCatalogTest() {
        config = new LdapConfig();
        config.setServer("10.4.6.12");
        config.setPort(3269);
        config.setBaseDN("dc=tba,dc=ebf,dc=de");
        config.setUsername("cn=Administrator,cn=Users,dc=tba,dc=ebf,dc=de");
        config.setPassword("Eo2WUgNw");
        config.setType(LdapType.ActiveDirectory);
    }
    
    @Test
    public void getUserBySamAccountName() throws LdapException {
        LdapUser user = userManager.getUser(ACTIVE_DIRECTORY_SAM_ACCOUNT_NAME, config);
        assert(user!=null);
    }
    
    @Test
    public void getUserByUserPrinciplaName() throws LdapException {
        LdapUser user = userManager.getUser(ACTIVE_DIRECTORY_USER_PRINCIPAL_NAME, config);
        assert(user!=null);
    }
}
