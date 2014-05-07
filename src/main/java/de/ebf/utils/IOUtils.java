package de.ebf.utils;

import de.ebf.constants.BaseConstants;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class IOUtils {

    private static final Logger log = Logger.getLogger(IOUtils.class);
    
   public static int saveToDisk(InputStream inputStream, String path) throws IOException {
      BufferedInputStream bis = new BufferedInputStream(inputStream);
      FileOutputStream fis = new FileOutputStream(path);
      int readBytes;
      int totalBytes = 0;
      try {
        byte[] buffer = new byte[10000];
        while ((readBytes = bis.read(buffer, 0, 10000)) != -1) {
           fis.write(buffer, 0, readBytes);
           totalBytes += readBytes;
        }
      } finally {
        fis.close();
        bis.close();
      }
      return totalBytes;
   }

   public static void deleteFromDisk(String path) {
       boolean success = new File(path).delete();
       if (!success){
           log.warn("Unable to delete file "+path);
       }
   }

   public static String readFile(String string) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtils.class.getClassLoader().getResourceAsStream(string), BaseConstants.UTF8));
      StringBuilder buffer = new StringBuilder();
      try {
        String line;
        while ((line = reader.readLine()) != null) {
           buffer.append(line);
        }
      } finally {
          reader.close();
      }
      return buffer.toString();
   }

   public static String getSimpleContentType(String contentType) {
      if (!StringUtils.isEmpty(contentType) && contentType.contains("/")) {
         return contentType.substring(contentType.indexOf("/") + 1);
      }
      return contentType;
   }
}
