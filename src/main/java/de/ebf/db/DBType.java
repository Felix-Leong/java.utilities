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

    MySQL("MySQL",
          "com.mysql.jdbc.Driver",
          "org.hibernate.dialect.MySQLDialect",
          "jdbc:mysql://",
          "SELECT 1 from dual",
          "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()",
          "SELECT COLUMN_NAME, DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME = :tableName"),
    
    MSSQL("Microsoft SQL Server",
          "net.sourceforge.jtds.jdbc.Driver",
          "org.hibernate.dialect.SQLServerDialect",
          "jdbc:jtds:sqlserver://",
          "SELECT GETDATE()",
          "SELECT TABLE_NAME FROM information_schema.TABLES",
          "SELECT TABLE_NAME, DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = :tableName"); 

    private String label;
    private String driverClass;
    private String dialect;
    private String urlPrefix;
    private String testQuery;
    private String tableQuery;
    private String columnQuery;

    DBType(String label, String driverClass, String dialect, String urlPrefix, String testQuery, String tableQuery, String columnQuery) {
        this.label = label;
        this.driverClass = driverClass;
        this.dialect = dialect;
        this.urlPrefix = urlPrefix;
        this.testQuery = testQuery;
        this.tableQuery = tableQuery;
        this.columnQuery = columnQuery;
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

    public String getTableQuery() {
        return tableQuery;
    }

    public void setTableQuery(String tableQuery) {
        this.tableQuery = tableQuery;
    }

    public String getColumnQuery() {
        return columnQuery;
    }

    public void setColumnQuery(String columnQuery) {
        this.columnQuery = columnQuery;
    }
}
