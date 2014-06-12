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
public class DominoSchema extends LdapSchema{
    
    //used as a login name
    public static final String ATTR_LOGIN_MAIL     = "mail";
    
    public DominoSchema(){
        super();
        this.ATTR_OBJECTCLASS           = "objectClass";
        this.ATTR_ENTRYUUID             = "dominoUNID";
        this.ATTR_DN                    = "dn";
        this.ATTR_MEMBERS               = "member";
        
        
        //this.ATTR_MEMBERS               = "member"; //could also be uniqueMember, not sure
        //this.ATTR_IS_MEMBER_OF          = "dominoaccessgroups";
        
        super.updateAttributes();
    }
    
}
