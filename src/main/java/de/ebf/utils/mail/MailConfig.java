/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.mail;

/**
 *
 * @author dominik
 */
public interface MailConfig {

    String getBccEmail();

    String getHost();

    String getPassword();

    Integer getPort();

    String getSenderEmail();

    String getSenderName();

    String getUsername();
    
    String getProtocol();

    Boolean getRequireAuthentication();

    void setBccEmail(String bccEmail);

    void setHost(String host);

    void setPassword(String password);

    void setPort(Integer port);

    void setRequireAuthentication(Boolean requireAuthentication);

    void setSenderEmail(String senderEmail);

    void setSenderName(String senderName);

    void setUsername(String username);
    
    void setProtocol(String protocol);
    
}
