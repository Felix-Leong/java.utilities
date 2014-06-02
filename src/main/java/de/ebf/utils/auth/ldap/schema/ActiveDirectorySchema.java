/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.auth.ldap.schema;

import java.util.ArrayList;
import java.util.Arrays;


/**
 *
 * @author dominik
 */
public class ActiveDirectorySchema extends LdapSchema {
    
    //Active Diretory only attributes
    public static final String ATTR_USER_ACCOUNT_CONTROL    = "userAccountControl"; //exists only in AD
    public static final String ATTR_PRIMARY_GROUP_ID        = "primaryGroupId";
    public static final String ATTR_SAM_ACCOUNT_NAME        = "sAMAccountName";
    public static final String ATTR_USER_PRINCIPAL_NAME     = "userPrincipalName";
    
    public ActiveDirectorySchema(){
        super();
        this.OBJECTCLASS_USER           = "user";
        this.OBJECTCLASS_GROUP          = "group";
        this.ATTR_ENTRYUUID             = "objectGUID";
        this.ATTR_UID                   = "objectSid";
        this.ATTR_DN                    = "distinguishedName";
        this.ATTR_MEMBERS               = "member";
        //this.ATTR_IS_MEMBER_OF          = "memberOf";
        this.ATTR_USER_PW               = "unicodePwd";
        
        super.updateAttributes();
        
        ArrayList<String> allAttributes = new ArrayList<>(Arrays.asList(ATTR_ALL));
        allAttributes.add(ATTR_PRIMARY_GROUP_ID);
        allAttributes.add(ATTR_SAM_ACCOUNT_NAME);
        allAttributes.add(ATTR_USER_PRINCIPAL_NAME);
        allAttributes.toArray(ATTR_ALL);
    }
}
