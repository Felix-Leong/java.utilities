/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.mail;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author xz
 */
public class SMTPMailConfig implements Serializable, MailConfig{
    
    private static final long serialVersionUID = 1L;

    private String host;
    private Integer port;
    private String senderEmail;
    private String senderName;
    private String bccEmail;
    private Boolean requireAuthentication;
    private String username;
    private String password;
    private String protocol;
    
    @Override
    public String getBccEmail() {
        return bccEmail;
    }

    @Override
    public void setBccEmail(String bccEmail) {
        this.bccEmail = bccEmail;
    }
    
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getSenderEmail() {
        return senderEmail;
    }

    @Override
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    @Override
    public String getSenderName() {
        return senderName;
    }

    @Override
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public Boolean getRequireAuthentication() {
        return requireAuthentication;
    }

    @Override
    public void setRequireAuthentication(Boolean requireAuthentication) {
        this.requireAuthentication = requireAuthentication;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.host);
        hash = 53 * hash + Objects.hashCode(this.port);
        hash = 53 * hash + Objects.hashCode(this.senderEmail);
        hash = 53 * hash + Objects.hashCode(this.senderName);
        hash = 53 * hash + Objects.hashCode(this.bccEmail);
        hash = 53 * hash + Objects.hashCode(this.requireAuthentication);
        hash = 53 * hash + Objects.hashCode(this.username);
        hash = 53 * hash + Objects.hashCode(this.password);
        hash = 53 * hash + Objects.hashCode(this.protocol);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SMTPMailConfig other = (SMTPMailConfig) obj;
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (!Objects.equals(this.port, other.port)) {
            return false;
        }
        if (!Objects.equals(this.senderEmail, other.senderEmail)) {
            return false;
        }
        if (!Objects.equals(this.senderName, other.senderName)) {
            return false;
        }
        if (!Objects.equals(this.bccEmail, other.bccEmail)) {
            return false;
        }
        if (!Objects.equals(this.requireAuthentication, other.requireAuthentication)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return Objects.equals(this.protocol, other.protocol);
    }
}
