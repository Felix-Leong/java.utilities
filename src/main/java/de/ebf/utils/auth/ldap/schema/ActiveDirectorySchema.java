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
    
    public ActiveDirectorySchema(){
        super();
        this.OBJECTCLASS_USER           = "user";
        this.OBJECTCLASS_GROUP          = "group";
        this.ATTR_ENTRYUUID             = "objectGUID";
        this.ATTR_DN                    = "distinguishedName";
        this.ATTR_MEMBERS               = "member";
        this.ATTR_USER_PW               = "unicodePwd";
        this.ATTR_USER_ACCOUNT_CONTROL  = "userAccountControl";
        super.updateAttributes();
    }
}
