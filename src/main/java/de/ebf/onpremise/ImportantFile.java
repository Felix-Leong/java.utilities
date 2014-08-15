package de.ebf.onpremise;

import java.io.File;

/**
 * important files in the web application.
 * @author xz
 */
enum ImportantFile {

    VIRTUAL_HOST_DIR("${VirtualHost}"),
    JDBC_PROPERTIES_FILE_IN_VIRTUAL_HOST_DIR("${VirtualHost}/jdbc.properties"),  
    LOCAL_SETTINGS_PROPERTIES_FILE_IN_VIRTUAL_HOST_DIR("${VirtualHost}/LocalSettings.properties"),
    
    ROOT_DIR("${VirtualHost}${WebAppRoot}"),
    JDBC_PROPERTIES_FILE_IN_ROOT("${VirtualHost}${WebAppRoot}/WEB-INF/jdbc.properties"),
    LOCAL_SETTINGS_PROPERTIES_FILE_IN_ROOT("${VirtualHost}${WebAppRoot}/WEB-INF/classes/LocalSettings.properties"),
    
    WEBINF_DIR("${VirtualHost}${WebAppRoot}/WEB-INF"),
    CLASSES_DIR("${VirtualHost}${WebAppRoot}/WEB-INF/classes"),
    WEB_XML("${VirtualHost}${WebAppRoot}/WEB-INF/web.xml"),
    ;

    private String path;

    ImportantFile(String path) {
        this.path = path.replace("${VirtualHost}", OnpremiseUtil.getVirtualHostDirPath())
                .replace("${WebAppRoot}", getWebAppRoot(OnpremiseUtil.getContextPath()));
    }
    
    public String getFilePath() {
        return path;
    }
    
    public File getFile() {
        return new File(getFilePath());
    }
    
    public String getWebAppRoot(String contextPath) {
        String webAppRoot = "/ROOT"; //default web root for tomcat
        if(contextPath!=null && !contextPath.trim().equals("")) {
            webAppRoot = contextPath.trim();
        }
        return webAppRoot;
    }

}