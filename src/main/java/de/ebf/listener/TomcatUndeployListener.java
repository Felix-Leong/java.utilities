/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.listener;

import de.ebf.utils.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author dominik
 */
public class TomcatUndeployListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(TomcatUndeployListener.class);

    //flag to indicate that the application should not be deleted. Used by restart mechism implemented in the onPremis setup
    private static Boolean deleteContextFile = true;
    
    private static Boolean deleteLogFiles = true;

    PropertiesConfiguration config = Config.getInstance();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        //empty
    }

    /**
     * http://stackoverflow.com/questions/16702011/tomcat-deploying-the-same-application-twice-in-netbeans
     * tomcat workaround bug, in development mode, if tomcat is stopped and
     * application is not un-deployed, the old application will start up again
     * on startup, and then the new code will be deployed, leading to a the app
     * starting two times and introducing subtle bugs, when this app is stopped
     * and in dev mode remove the deployment descriptor from catalina base
     * @param event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
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
                if (!success){
                    log.warn("Unable to delete "+conextConfigFile);
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
    
    private List<File> getAllFiles(File file){
        List<File> allFiles = new ArrayList<>();
        if (file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null) { //some JVMs return null for empty dirs
                for(File f: files) {
                    if(f.isFile()) {
                        allFiles.add(f);
                    } else {
                        allFiles.addAll(getAllFiles(f));
                    }
                }
            }
        }
        return allFiles;
    }
    
    private List<File> getAllFolders(File file){
        List<File> allFolders = new ArrayList<>();
        if (file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null) { //some JVMs return null for empty dirs
                for(File f: files) {
                    if(f.isDirectory()) {
                        allFolders.addAll(getAllFolders(f));
                        allFolders.add(f);
                    }
                }
            }
        }
        return allFolders;
    }
    
    private void deleteOnExit(List<File> files){
        for (File file : files) {
            file.deleteOnExit();
        }
    }
    
    public static void setDeleteContextFile(boolean deleteContextFile){
        TomcatUndeployListener.deleteContextFile = deleteContextFile;
    }
}
