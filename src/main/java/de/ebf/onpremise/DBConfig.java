package de.ebf.onpremise;

/**
 *
 * @author xz
 */
public class DBConfig {

    private DBType type;
    private String host;
    private int port;
    private String dbName;
    private String userName;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
