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
    
    ROOT_DIR("${VirtualHost}/ROOT"),
    JDBC_PROPERTIES_FILE_IN_ROOT("${VirtualHost}/ROOT/WEB-INF/jdbc.properties"),
    LOCAL_SETTINGS_PROPERTIES_FILE_IN_ROOT("${VirtualHost}/ROOT/WEB-INF/classes/LocalSettings.properties"),
    
    WEBINF_DIR("${VirtualHost}/ROOT/WEB-INF"),
    CLASSES_DIR("${VirtualHost}/ROOT/WEB-INF/classes"),
    WEB_XML("${VirtualHost}/ROOT/WEB-INF/web.xml"),
    ;

    private String path;

    ImportantFile(String path) {
        this.path = path.replace("${VirtualHost}", OnpremiseUtil.getVirtualHostDirPath());
    }
    
    public String getFilePath() {
        return path;
    }
    
    public File getFile() {
        return new File(getFilePath());
    }

}