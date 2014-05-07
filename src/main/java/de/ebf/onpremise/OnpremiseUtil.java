/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.onpremise;

import de.ebf.filter.LoginFilter;
import de.ebf.listener.TomcatUndeployListener;
import de.ebf.utils.Config;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *
 * @author xz
 */
public class OnpremiseUtil {

    private static Boolean isOnpremise;
    private static final Logger log = Logger.getLogger(OnpremiseUtil.class);
    private static String rootAbsolutePath;
    private static ServletContext servletContext;
    private static boolean isPropertiesSetupFinished = false;
    private static String virutalHosAbsolutePath;
    
    public static boolean isPropertiesSetupFinished() {
        return isPropertiesSetupFinished;
    }

    public static void setIsPropertiesSetupFinished(boolean pIsPropertiesSetupFinished) {
        isPropertiesSetupFinished = pIsPropertiesSetupFinished;
        log.debug("isPropertiesSetupFinished is set to be "+isPropertiesSetupFinished);
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(ServletContext servletContext) {
        OnpremiseUtil.servletContext = servletContext;
    }

    public static synchronized boolean isSetupRequired() {
        if(isOnpremise==null) {
            isOnpremise = Config.getInstance().getString("app.require.setup").equalsIgnoreCase("true");
        }
        return isOnpremise;
    }

    /**
     * check whether the two properties file are existing in the virtual host folder
     * @return 
     */
    public static boolean existPropertiesFilesInVirutalHostDir() {
        return ImportantFile.JDBC_PROPERTIES_FILE_IN_VIRTUAL_HOST_DIR.getFile().exists()
                    && ImportantFile.LOCAL_SETTINGS_PROPERTIES_FILE_IN_VIRTUAL_HOST_DIR.getFile().exists();
    }
    

    /** 
     *  return the absolute path of the ROOT directory
     * @return 
     */
    public static String getRootAbsolutePath() {
        if (rootAbsolutePath == null && servletContext != null) {
            rootAbsolutePath = servletContext.getRealPath("/");
        }
        return rootAbsolutePath;
    }

    /**
     * return the ROOT dir as a File object
     *
     * @return
     */
    private static File getRootDir() {
        File rootDir = new File(getRootAbsolutePath());
        return rootDir;
    }

    /**
     * return the virtual host dir as a File object
     *
     * @return
     */
    private static File getVirutalHostDir() {
        return getRootDir().getParentFile();

    }
    /**
     * e.g   /Users/xz/Development/projects/telekom.toolbox.server/target
     * @return 
     */
    public static String getVirtualHostDirPath(){
       if (virutalHosAbsolutePath == null){
           virutalHosAbsolutePath = getVirutalHostDir().getPath();
        }
        return virutalHosAbsolutePath;
    }

    /**
     * Load the MainDispatcherServlet into the Tomcat servlet context. This servlet uses the configuration file "MainDispatcher-servlet.xml".
     */
    public static void addMainDispatcherServlet() {
        log.info("Starting to add the MainDispatcherServlet...........");
        Dynamic dynamic = servletContext.addServlet("MainDispatcher", DispatcherServlet.class);//MainDispatcherServlet.class);
        dynamic.addMapping("/");
        
        LoginFilter.setMainDispatcher(true);
    }
    
    

    /**
     * Load the SetupWizardDispatcher into the servlet context. This servlet uses the configuration file "SetupWizardDispatcher-servlet.xml".
     */
    public static void addSetupWizardDispatcherServlet() {
        log.info("Starting to add the SetupWizardDispatcher...........");
        Dynamic dynamic = servletContext.addServlet("SetupWizardDispatcher", DispatcherServlet.class);//SetupWizardDispatcherServlet.class);
        dynamic.addMapping("/");
        
        LoginFilter.setMainDispatcher(true);
    }
    
    /**
     * Restart  the web application(servlet context) to lead to the invocation of the contextInitialized() method of 
     * OnpremiseContextListener, after the properties file is finished with setup.
     */
    public static void restartWebApplication(){
        try {
            TomcatUndeployListener.setDeleteContextFile(false);
            //touch the web.xml to ask tomcat to restart this web application
            FileUtils.touch(ImportantFile.WEB_XML.getFile());
        } catch (IOException ex) {
            log.error("Failure in restarting the web application!!");
        }
    }

    /**
     * For each start of the web application, it will be checked if jdbc.properties and LocalSettings.properties are 
     * existing in the virtual host directory. If yes, copy the jdbc.properties to /WEB-INF and LocalSettings.properties to /WEB-INF/classes.
     * The reason that we do this way is that the ROOT folder including the properties files, which are configured with the setupwizard, are deleted 
     * every time the deployment version is updated. 
     * 
     */
    public static void copyPropertiesFileFromVirtualHostDirToRoot() {
        try {
            FileUtils.copyFileToDirectory(
                    ImportantFile.JDBC_PROPERTIES_FILE_IN_VIRTUAL_HOST_DIR.getFile(),
                    ImportantFile.WEBINF_DIR.getFile()
            );
            FileUtils.copyFileToDirectory(
                    ImportantFile.LOCAL_SETTINGS_PROPERTIES_FILE_IN_VIRTUAL_HOST_DIR.getFile(),
                    ImportantFile.CLASSES_DIR.getFile()
            );
        } catch (IOException ex) {
            log.error("Errors happened in copying the properties files from the virtual host dir to application root dir!!!", ex);
        }
    }
    
    
    /**
     * copy the 2 properties files from ROOT directory or ROOT/classes directory to virtual host directory .
     */
    public static void copyPropertiesFileFromRootToVirtualHostDir(){
        try {
            FileUtils.copyFileToDirectory(
                    ImportantFile.JDBC_PROPERTIES_FILE_IN_ROOT.getFile(),
                    ImportantFile.VIRTUAL_HOST_DIR.getFile()
            );

            FileUtils.copyFileToDirectory(
                    ImportantFile.LOCAL_SETTINGS_PROPERTIES_FILE_IN_ROOT.getFile(),
                    ImportantFile.VIRTUAL_HOST_DIR.getFile()
            );
        } catch (IOException ex) {
            log.error("Errors happened in copying the properties files from root dir to virtual host dir !!!", ex);
        }
    }
    
    
    /**
     * To check whether the spring root application context has been existing.
     * @return
     */
    public static boolean existRootApplicationContext() {
        return (getRootApplicationContext() != null);
    }

    public static WebApplicationContext getRootApplicationContext() {

        Object attr = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        if (attr != null && attr instanceof WebApplicationContext) {
            return (WebApplicationContext) attr;
        }
        return null;

    }


}




