/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.listener;

import de.ebf.utils.Config;
import de.ebf.utils.auth.ldap.LdapUtil;
import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * A better name for this class is TomcatShutdownListener. Because this class now contains 
 * several operations which should be executed when tomcat is shutdown, rather than just the 
 * operation of removing old deployment descriptor.
 * @author dominik
 */
public class TomcatShutdownListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(TomcatShutdownListener.class);

    //flag to indicate that the application should not be deleted. Used by restart mechism implemented in the onPremis setup
    private static Boolean deleteContextFile = true;

    private static Boolean deleteLogFiles = true;

    PropertiesConfiguration config = Config.getInstance();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        //empty
    }


    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        //Remove the old deployment descriptor from Tomcat folder in development mode to avoid two times of app-starting.
        removeOldDeploymentDescriptor(event);

        //Close all LDAP connections in connection pools to prevent memory leak.
        LdapUtil.closeAllLDAPConnectionPools();

        //Deregister jdbc driver to solve the error log in catalina.log. 
        deregisterJdbcDrivers();
    }
    
    
    /**
     * http://stackoverflow.com/questions/16702011/tomcat-deploying-the-same-application-twice-in-netbeans
     * tomcat workaround bug, in development mode, if tomcat is stopped and
     * application is not un-deployed, the old application will start up again
     * on startup, and then the new code will be deployed, leading to a the app
     * starting two times and introducing subtle bugs, when this app is stopped
     * and in dev mode remove the deployment descriptor from catalina base
     *
     * @param event
     */
    private void removeOldDeploymentDescriptor(ServletContextEvent event) {
        String contextPath = event.getServletContext().getContextPath();
        final String catalinaBase = System.getProperty("catalina.base");

        if (contextPath.equals("")) {
            contextPath = "ROOT";
        }

        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

        if (isDebug && deleteContextFile) {
            final File conextConfigFile = new File(catalinaBase, "conf/Catalina/localhost/" + contextPath + ".xml");
            if (conextConfigFile.exists() && conextConfigFile.canRead()) {
                log.info("Deleting " + conextConfigFile.getAbsolutePath() + " to prevent duplicate deployment at next startup.");
                boolean success = conextConfigFile.delete();
                if (!success) {
                    log.warn("Unable to delete " + conextConfigFile);
                }
            }
        }

        if (isDebug && deleteLogFiles) {
            final File logDir = new File(catalinaBase, "logs");
            if (logDir.exists() && logDir.isDirectory()) {
                log.info("Scheduling for deletion at JVM shutdown: " + logDir.getAbsolutePath());
                List<File> allFiles = getAllFiles(logDir);
                List<File> allFolders = getAllFolders(logDir);

                //specify folders and files to delete in reverse order
                deleteOnExit(allFolders);
                deleteOnExit(allFiles);
            }
        }
    }

    /**
     * Solve the error complained in the log file catalina20140908.log ---> 
     *      org.apache.catalina.loader.WebappClassLoader.clearReferencesJdbc The web application [] registered the JDBC driver [com.mysql.jdbc.Driver] 
     *      but failed to unregister it when the web application was stopped. To prevent a memory leak, the JDBC Driver has been forcibly unregistered.
     */
    private void deregisterJdbcDrivers() {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.info("Deregistered JDBC driver : "+driver);
            } catch (Exception e) {
                log.warn("Error in deregistering JDBC driver : " + driver);
            }
        }
    }

    private List<File> getAllFiles(File file) {
        List<File> allFiles = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (f.isFile()) {
                        allFiles.add(f);
                    } else {
                        allFiles.addAll(getAllFiles(f));
                    }
                }
            }
        }
        return allFiles;
    }

    private List<File> getAllFolders(File file) {
        List<File> allFolders = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (f.isDirectory()) {
                        allFolders.addAll(getAllFolders(f));
                        allFolders.add(f);
                    }
                }
            }
        }
        return allFolders;
    }

    private void deleteOnExit(List<File> files) {
        for (File file : files) {
            file.deleteOnExit();
        }
    }

    public static void setDeleteContextFile(boolean deleteContextFile) {
        TomcatShutdownListener.deleteContextFile = deleteContextFile;
    }
}
