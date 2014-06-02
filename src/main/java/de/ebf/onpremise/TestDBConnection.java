/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.onpremise;

import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 *
 * @author xz
 */
public class TestDBConnection {

    private static final Logger log = Logger.getLogger(TestDBConnection.class);

    public static boolean testDB(DBConfig dbConfig) throws HibernateException {

        boolean dbOK = false;
        Properties properties = new Properties();

        properties.setProperty("hibernate.connection.driver_class", dbConfig.getType().getDriverClass());
        properties.setProperty("hibernate.connection.url", dbConfig.getUrl());
        properties.setProperty("hibernate.dialect", dbConfig.getType().getDialect());
        properties.setProperty("hibernate.connection.username", dbConfig.getUserName());
        properties.setProperty("hibernate.connection.password", dbConfig.getPassword());

        properties.setProperty("hibernate.current_session_context_class", "thread");//bound the current session to this thread.
        
        Configuration configuration = new Configuration().setProperties(properties);
        
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

//        SessionFactory factory = new Configuration().setProperties(properties).buildSessionFactory();
        SessionFactory factory = configuration.buildSessionFactory(serviceRegistry);
        Session session = factory.getCurrentSession();

        org.hibernate.Transaction tx = session.beginTransaction();
        Query query = session.createSQLQuery(dbConfig.getType().getTestQuery());
        List list = query.list();
        if (list != null && list.size() > 0) {
            dbOK = true;

        }
        tx.commit();

        return dbOK;
    }

}
