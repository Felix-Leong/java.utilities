/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.auth.ldap.schema;


/**
 *
 * @author dominik
 */
public class ActiveDirectorySchema extends LdapSchema {
    
    public static LdapSchema getInstance(){
        LdapSchema instance = LdapSchema.getInstance();
        instance.OBJECTCLASS_USER     = "user";
        instance.OBJECTCLASS_GROUP = "group";
        instance.ATTR_ENTRYUUID       = "objectGUID";
        instance.ATTR_DN              = "distinguishedName";
        instance.ATTR_MEMBERS         = "member";
        instance.ATTR_USER_PW         = "unicodePwd";
        instance.updateAllAttributes();
        return instance;
    }
}
