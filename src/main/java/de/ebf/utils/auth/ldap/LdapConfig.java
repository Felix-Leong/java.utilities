/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.auth.ldap;

import java.io.Serializable;

/**
 *
 * @author dominik
 */
public class LdapConfig implements Serializable {
    
    private LdapType Type;
    private String server;
    private Integer port;
    private String username;
    private String password;
    private String baseDN;

    
    public LdapType getType() {
        return Type;
    }

    public void setType(LdapType Type) {
        this.Type = Type;
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
}
