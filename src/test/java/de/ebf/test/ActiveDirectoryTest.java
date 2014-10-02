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
public class ActiveDirectoryTest extends LDAPWriteTest {

    private static final String ACTIVE_DIRECTORY_ADMIN_USER_NAME            = "Administrator";
    private static final String ACTIVE_DIRECTORY_ADMIN_GROUP_NAME           = "Administrators";
    private static final String ACTIVE_DIRECTORY_DOMAIN_USERS_GROUP_NAME    = "Domain Users";
    
    public ActiveDirectoryTest() {
        config = new LdapConfig();
        config.setServer("10.4.6.12");
        config.setPort(636);
        config.setViaSSL(Boolean.TRUE);
        config.setBaseDN("dc=tba,dc=ebf,dc=de");
        config.setUsername("cn=LDAPTest,cn=Users,dc=tba,dc=ebf,dc=de");
        config.setPassword("NjI9MkO0");
        config.setType(LdapType.ActiveDirectory);
    }

    @Test
    public void test11_getAdminMember() throws LdapException {
        LdapUser user = ldapUserManager.getUser(ACTIVE_DIRECTORY_ADMIN_USER_NAME, config);
        LdapGroup group = ldapGroupManager.getGroup(ACTIVE_DIRECTORY_ADMIN_GROUP_NAME, true, config);
        assert (group.getMembers() != null && group.getMembers().size() > 0);
        assert (group.getMembers().contains(user));
    }
    
    //Group membership for the domain admins group is determined differently
    @Test
    public void test12_getDomainUsersMember() throws LdapException {
        LdapUser user = ldapUserManager.getUser(ACTIVE_DIRECTORY_ADMIN_USER_NAME, config);
        LdapGroup group = ldapGroupManager.getGroup(ACTIVE_DIRECTORY_DOMAIN_USERS_GROUP_NAME, true, config);
        assert (group.getMembers() != null && group.getMembers().size() > 0);
        assert (group.getMembers().contains(user));
    }
}
