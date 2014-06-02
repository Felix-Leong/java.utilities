package de.ebf.onpremise;

import java.io.Serializable;

/**
 *
 * @author xz
 */
public class DBConfig implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private DBType type;
    private String host;
    private int port;
    private String dbName;
    private String username;
    private String password;

    public DBType getType() {
        return type;
    }

    public void setType(DBType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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
    
    public String getUrl() {
        return this.getType().getUrlPrefix() + this.getHost() + ":" + this.getPort() + "/" + this.getDbName();
    }

}
