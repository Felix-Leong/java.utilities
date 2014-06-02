/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.onpremise;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;

/**
 * You can call this class to load the root web application context of spring at
 * the time you want.
 *
 * @author xz
 */
public class SpringRootContextLoader {

    private static final Logger log = Logger.getLogger(SpringRootContextLoader.class);
    
    private static SpringRootContextLoader instance = null;
    private ContextLoader contextLoader = null;
    private ServletContext servletContext = null;

    private SpringRootContextLoader(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * get the singleton instance.
     * @return SpringRootContextLoader
     */
    public static synchronized SpringRootContextLoader getInstance() {

        if (instance == null) {
            ServletContext servletContext = OnpremiseUtil.getServletContext();
            if (servletContext == null) {
                log.error("servletContext is null. Please firstly set the ServletContext!");
                return null;
            }
            instance = new SpringRootContextLoader(servletContext);
        }
        return instance;
    }

    /**
     * ContextLoader will create and initialize the web application context, and
     * put it in the ServletContext as a attribute with attribute name whose
     * value is same as
     * WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
     */
    public void loadContext() {
        if (!OnpremiseUtil.existRootApplicationContext()) {
            log.info("Starting to load the root application context...");
            getContextLoader().initWebApplicationContext(servletContext);
        } else {
            log.info("Spring root web app context has been existing. It can not be loaded again!");
        }

    }
 
    public void destroyContext() {
        if (this.contextLoader != null) {
            this.contextLoader.closeWebApplicationContext(servletContext);
            log.info("Spring app context is destroied!");
        }
    }

    public void reloadContext() {
        log.info("Reloading the root appliation context.....");
        destroyContext();
        loadContext();
    }



    private ContextLoader getContextLoader() {
        if (contextLoader == null) {
            contextLoader = new ContextLoader();
        }
        return contextLoader;
    }

}
