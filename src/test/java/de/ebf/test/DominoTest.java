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
public class DominoTest extends LDAPReadTest {

    private static final String DOMINO_EMAIL_USER_NAME = "ldap@ebf-dev.de";

    private static final String TEST_USER_NAME = "Ldap Search";
    private static final String TEST_GROUP_NAME = "LocalDomainAdmins";

    public DominoTest() {
        config = new LdapConfig();
        config.setServer("10.4.7.10");
        config.setPort(1389);//1636 for SSL
        config.setBaseDN("");
        config.setUsername("CN=IBM Admin,O=EBF-DEV");
        config.setPassword("domino");
        config.setType(LdapType.Domino);
    }

    @Test
    public void test02_getUser() throws LdapException {
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        assert (user != null);
    }
    
    @Test
    public void test03_getGroup() throws LdapException {
        LdapGroup group = ldapGroupManager.getGroup(TEST_GROUP_NAME, false, config);
        assert (group != null);
    }

    @Test
    public void test04_getMember() throws LdapException {
        LdapUser user = ldapUserManager.getUser(TEST_USER_NAME, config);
        LdapGroup group = ldapGroupManager.getGroup(TEST_GROUP_NAME, true, config);
        assert (group.getMembers() != null && group.getMembers().size() > 0);
        assert (group.getMembers().contains(user));
    }

    @Test
    public void test05_getUserByEmail() throws LdapException {
        LdapUser user = ldapUserManager.getUser(DOMINO_EMAIL_USER_NAME, config);
        assert (user != null);
    }
}
