/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.test;

import de.ebf.utils.auth.ldap.LdapException;
import de.ebf.utils.auth.ldap.LdapGroup;
import de.ebf.utils.auth.ldap.LdapType;
import de.ebf.utils.auth.ldap.LdapUser;
import de.ebf.utils.auth.ldap.config.LdapConfig;
import org.junit.Test;

/**
 *
 * @author dominik
 */
public class ActiveDirectoryGlobalCatalogTest extends LDAPReadTest {

    private static final String TEST_USER_NAME = "Administrator";
    private static final String TEST_GROUP_NAME = "Administrators";

    private static final String ACTIVE_DIRECTORY_SAM_ACCOUNT_NAME = "sAMAccountName.Test";
    private static final String ACTIVE_DIRECTORY_USER_PRINCIPAL_NAME = "sAMAccountName.Test@tba.ebf.de";

    public ActiveDirectoryGlobalCatalogTest() {
        config = new LdapConfig();
        config.setServer("10.4.6.12");
        config.setPort(3269);
        config.setViaSSL(Boolean.TRUE);
        config.setBaseDN("dc=tba,dc=ebf,dc=de");
        config.setUsername("cn=LDAPTest,cn=Users,dc=tba,dc=ebf,dc=de");
        config.setPassword("NjI9MkO0");
        config.setType(LdapType.ActiveDirectory);
    }

    @Test
    public void getUser() throws LdapException {
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        assert (user != null);
    }

    @Test
    public void getGroup() throws LdapException {
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        assert (group != null);
    }

    @Test
    public void getMember() throws LdapException {
        LdapUser user = userManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = groupManager.getGroup(TEST_GROUP_NAME, config);
        assert (group.getMembers() != null && group.getMembers().size() > 0);
        assert (group.getMembers().contains(user));
    }

    @Test
    public void getUserBySamAccountName() throws LdapException {
        LdapUser user = userManager.getUser(ACTIVE_DIRECTORY_SAM_ACCOUNT_NAME, config);
        assert (user != null);
    }

    @Test
    public void getUserByUserPrinciplaName() throws LdapException {
        LdapUser user = userManager.getUser(ACTIVE_DIRECTORY_USER_PRINCIPAL_NAME, config);
        assert (user != null);
    }
}
