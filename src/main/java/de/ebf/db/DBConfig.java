package de.ebf.db;

import java.io.Serializable;

/**
 *
 * @author xz
 */
public class DBConfig implements Serializable, DBInterface {
    
    private static final long serialVersionUID = 1L;

    private DBType type;
    private String host;
    private Integer port;
    private String dbName;
    private String username;
    private String password;
    private String query;

    @Override
    public DBType getDatabaseType() {
        return type;
    }

    public void setType(DBType type) {
        this.type = type;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String getUrl() {
        return this.getDatabaseType().getUrlPrefix() + this.getHost() + ":" + this.getPort() + "/" + this.getDbName();
    }
    
    @Override
    public String getQuery() {
        return this.query;
    }
}
