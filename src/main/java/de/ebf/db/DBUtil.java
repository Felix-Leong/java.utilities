/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.db;

import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 *
 * @author xz
 */
public class DBUtil {

    private static final Logger log = Logger.getLogger(DBUtil.class);

    public static void testDB(DBInterface db) throws Exception {
        if (db.getType() == null){
            throw new Exception("Please choose a DB Type");
        }

        Session session = getSession(db);
        Transaction tx = session.beginTransaction();
        Query query = session.createSQLQuery(db.getType().getTestQuery());
        List list = query.list();
        if (list == null && list.size() <= 0) {
            throw new Exception("Test query did not execute successfully.");
        }
        tx.commit();
        
    }
    
    public static Session getSession(DBInterface db){
        Properties properties = new Properties();

        properties.setProperty("hibernate.connection.driver_class", db.getType().getDriverClass());
        properties.setProperty("hibernate.connection.url", db.getUrl());
        properties.setProperty("hibernate.dialect", db.getType().getDialect());
        properties.setProperty("hibernate.connection.username", db.getUsername());
        properties.setProperty("hibernate.connection.password", db.getPassword());
        properties.setProperty("hibernate.current_session_context_class", "thread");//bound the current session to this thread.
        
        Configuration configuration = new Configuration().setProperties(properties);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        SessionFactory factory = configuration.buildSessionFactory(serviceRegistry);
        return factory.getCurrentSession();
    }

}
