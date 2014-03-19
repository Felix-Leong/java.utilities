/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.listener;

import de.ebf.utils.Config;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
/**
 *
 * @author dominik
 */
public class TomcatUndeployListener implements ServletContextListener {
   
   private static final Logger log = Logger.getLogger(TomcatUndeployListener.class);
   
   //flag to indicate that the application should not be deleted. Used by restart mechism implemented in the onPremis setup
   public static Boolean DELETE_CONTEXT_FILE = true;
   
   PropertiesConfiguration config = Config.instance;
    
   @Override
   public void contextInitialized(ServletContextEvent event) {
      //enmpty
   }

   /**
    * http://stackoverflow.com/questions/16702011/tomcat-deploying-the-same-application-twice-in-netbeans
    * tomcat workaround bug, in development mode, if tomcat is stopped and application is not un-deployed,
    * the old application will start up again on startup, and then the new code will be deployed, leading
    * to a the app starting two times and introducing subtle bugs, when this app is stopped and in dev mode
    * remove the deployment descriptor from catalina base
    */
   @Override
   public void contextDestroyed(final ServletContextEvent event) {
    String contextPath = event.getServletContext().getContextPath();
    final String catalinaBase = System.getProperty("catalina.base");
    
    if (contextPath.equals("")){
        contextPath = "ROOT";
    }

    boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
    
    if (isDebug && DELETE_CONTEXT_FILE) {
        final File conextConfigFile = new File(catalinaBase, "conf/Catalina/localhost/"+contextPath+".xml");
        if (conextConfigFile.exists() && conextConfigFile.canRead()) {
            log.info("Deleting "+conextConfigFile.getAbsolutePath()+" to prevent duplicate deployment at next startup.");
            conextConfigFile.delete();
        }
    }
   }
}
