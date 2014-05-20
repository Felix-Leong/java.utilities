/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.auth.ldap.config;

import de.ebf.utils.auth.ldap.LdapType;
import de.ebf.utils.auth.ldap.schema.LdapSchema;
import de.ebf.utils.auth.ldap.schema.LdapSchemaFactory;
import java.io.Serializable;

/**
 *
 * @author dominik
 */
public class LdapConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //primary LDAP server
    private LdapType type;
    private String server;
    private Integer port;
    private String username;
    private String password;
    private String baseDN;
    
    //secondary LDAP server
    private String server2;
    private Integer port2;
    
    public LdapType getType() {
        return type;
    }

    public void setType(LdapType Type) {
        this.type = Type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }
    
    public LdapSchema getSchema(){
        return LdapSchemaFactory.getLdapSchema(type);
    }

    public String getServer2() {
        return server2;
    }

    public void setServer2(String server2) {
        this.server2 = server2;
    }

    public Integer getPort2() {
        return port2;
    }

    public void setPort2(Integer port2) {
        this.port2 = port2;
    }
}
