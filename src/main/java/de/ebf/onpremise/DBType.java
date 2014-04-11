/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.onpremise;

/**
 *
 * @author xz
 */
public enum DBType {

    MySQL("MySQL", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", "jdbc:mysql://"), //mysql 
    MSSQL("Miscrosoft SQL Server", "net.sourceforge.jtds.jdbc.Driver", "org.hibernate.dialect.SQLServerDialect", "jdbc:microsoft:sqlserver://"); //microsot sql server

    private String label;
    private String driverClass;
    private String dialect;
    private String urlPrefix;
    public String getDialect() {
        return dialect;
    }
    
    DBType(String label, String driverClass, String dialect, String urlPrefix) {
        this.label = label;
        this.driverClass = driverClass;
        this.dialect = dialect;
        this.urlPrefix = urlPrefix;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public String getLabel() {
        return label;
    }

    public String getDriverClass() {
        return driverClass;
    }

}
