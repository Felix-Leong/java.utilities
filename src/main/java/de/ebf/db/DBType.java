/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.db;

/**
 *
 * @author xz
 */
public enum DBType {

    MySQL("MySQL", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", "jdbc:mysql://", "select 1 from dual"), 
    MSSQL("Miscrosoft SQL Server", "net.sourceforge.jtds.jdbc.Driver", "org.hibernate.dialect.SQLServerDialect", "jdbc:jtds:sqlserver://", "SELECT GETDATE()"); 

    private String label;
    private String driverClass;
    private String dialect;
    private String urlPrefix;
    private String testQuery;

    DBType(String label, String driverClass, String dialect, String urlPrefix, String testQuery) {
        this.label = label;
        this.driverClass = driverClass;
        this.dialect = dialect;
        this.urlPrefix = urlPrefix;
        this.testQuery = testQuery;
    }

    public String getDialect() {
        return dialect;
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

    public String getTestQuery() {
        return testQuery;
    }

}
