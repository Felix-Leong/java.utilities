/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.db;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.springframework.util.StringUtils;

/**
 *
 * @author xz
 */
public class DBUtil {

    private static final Logger log = Logger.getLogger(DBUtil.class);

    public static void testDB(DBInterface db) throws Exception {
        if (db.getDatabaseType()== null){
            throw new Exception("Please choose a DB Type");
        }

        Session session = getSession(db);
        Transaction tx = session.beginTransaction();
        Query query = session.createSQLQuery(db.getDatabaseType().getTestQuery());
        List list = query.list();
        if (list == null && list.size() <= 0) {
            throw new Exception("Test query did not execute successfully.");
        }
        tx.commit();
        
    }
    
    public static Session getSession(DBInterface db){
        Properties properties = new Properties();

        properties.setProperty("hibernate.connection.driver_class", db.getDatabaseType().getDriverClass());
        properties.setProperty("hibernate.connection.url", db.getUrl());
        properties.setProperty("hibernate.dialect", db.getDatabaseType().getDialect());
        properties.setProperty("hibernate.connection.username", db.getUsername());
        properties.setProperty("hibernate.connection.password", db.getPassword());
        properties.setProperty("hibernate.current_session_context_class", "thread");//bound the current session to this thread.
        
        Configuration configuration = new Configuration().setProperties(properties);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        SessionFactory factory = configuration.buildSessionFactory(serviceRegistry);
        return factory.getCurrentSession();
    }

    public static PreparedStatement getPreparedStatement(final DataSource dataSource, final String query) throws SQLException {
        if (!StringUtils.isEmpty(query)) {
            return dataSource.getConnection().prepareStatement(query);
        }
        return null;
    }

    public static DataSource getDataSource(DBInterface db) {
        DataSource dataSource = null;
        switch (db.getDatabaseType()) {
            case MySQL:
                MysqlDataSource ds = new MysqlDataSource();
                ds.setDatabaseName(db.getDbName());
                ds.setServerName(db.getHost());
                ds.setPort(db.getPort());
                ds.setUser(db.getUsername());
                ds.setPassword(db.getPassword());
                dataSource = ds;
                break;
            case MSSQL:
                JtdsDataSource jtds = new JtdsDataSource();
                jtds.setDatabaseName(db.getDbName());
                jtds.setServerName(db.getHost());
                jtds.setPortNumber(db.getPort());
                jtds.setUser(db.getUsername());
                jtds.setPassword(db.getPassword());
                dataSource = jtds;
                break;
        }
        return dataSource;
    }

}
