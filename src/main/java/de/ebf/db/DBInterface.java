/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.db;

/**
 *
 * @author dominik
 */
public interface DBInterface {

    String getDbName();

    String getHost();

    String getPassword();

    Integer getPort();

    DBType getType();

    String getUrl();

    String getUsername();
    
    String getQuery();
}
