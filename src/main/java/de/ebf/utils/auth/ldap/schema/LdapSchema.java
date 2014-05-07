/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.auth.ldap.schema;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author dominik
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "warning is incorrect")
public class LdapSchema {
    
    public String ATTR_USER_PW               = "userPassword";
    public String ATTR_OBJECTCLASS           = "objectclass";
    public String ATTR_LAST_NAME             = "sn";
    public String ATTR_MAIL                  = "mail";
    public String ATTR_ENTRYUUID             = "entryUUID";
    public String ATTR_FIRST_NAME            = "givenName";
    public String ATTR_CN                    = "cn";
    public String ATTR_UID                   = "uid";
    public String ATTR_TELEPHONE_NUMBER      = "telephoneNumber";
    public String ATTR_MEMBERS               = "uniqueMember";
    public String OBJECTCLASS_USER           = "inetOrgPerson";
    public String OBJECTCLASS_GROUP          = "groupOfUniqueNames";
    public String OBJECT_CLASS_OU            = "organizationalUnit";
    public String OBJECT_CLASS_ORGANIZATION  = "domain";
    public String ATTR_DN                    = "entryDN";
    public String ATTR_USER_ACCOUNT_CONTROL  = null; //exists only in AD
    public String[] ATTR_ALL                 = null;

    protected LdapSchema(){
        updateAttributes();
    }
    
    protected final void updateAttributes(){
        ATTR_ALL = new String[]{ATTR_CN, ATTR_DN, ATTR_FIRST_NAME, ATTR_LAST_NAME, ATTR_UID, ATTR_MAIL, ATTR_TELEPHONE_NUMBER, ATTR_ENTRYUUID, ATTR_MEMBERS};
    }
}
