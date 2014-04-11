package de.ebf.onpremise;

import de.ebf.utils.Config;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 * This class manages the loading of the spring root application context, MainDispatcher servlet and the SetupWizardDispatcher servlet.
 * This class listens to the INIT and DESTROY events of the servletContext of this web application.
 * @author xz
 */
public class OnpremiseContextListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(OnpremiseContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.debug("OnpremiseContextListener.contextInitialized() is being called.");
        OnpremiseUtil.setServletContext(sce.getServletContext());//<----important!!!!
        
        /*Check the build profile is onpremise or managed.
         If it is onpremise type, check if the properties file(JDBC.properties,localSetting.properties) exist in the parent folder.
         If yes, copy these two properties files and paste and overwrite in the Root folder. Start the root appliation context.
         If no, flag that setupwizard to setup the properties is needed.*/
        if (OnpremiseUtil.isSetupRequired()) {
            if (OnpremiseUtil.existPropertiesFilesInVirutalHostDir()) {
                /*If onpremise is true and properties files exist in parent dir, copy the 
                 existing properties files from parent dir to root dir.*/
                OnpremiseUtil.copyPropertiesFileFromVirtualHostDirToRoot();
                OnpremiseUtil.setIsPropertiesSetupFinished(true);
            } else {
                //If onpremise is true and the properties files in parent dir are not setup, we need to run setupWizard
                OnpremiseUtil.setIsPropertiesSetupFinished(false);
            }
        } 

        /*If it is not onpremise, or if it is onpremise and properties setup is finished, load the root 
        application context and the MainDispatcher servlet. Otherwise, load the SetupWizardDispatcher servlet
        for the setupWizard.*/
        if (!OnpremiseUtil.isSetupRequired() || OnpremiseUtil.isPropertiesSetupFinished()) {
            SpringRootContextLoader.getInstance().loadContext();
            OnpremiseUtil.addMainDispatcherServlet();
        } else {
            OnpremiseUtil.addSetupWizardDispatcherServlet();
        }

        Config.load(ImportantFile.CLASSES_DIR.getFilePath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        SpringRootContextLoader loader = SpringRootContextLoader.getInstance();
        if(loader!=null) {
            loader.destroyContext();
        }
        
    }

}
