package de.ebf.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.lang.StringUtils;

public class IOUtils {

   public static int saveToDisk(InputStream inputStream, String path) throws IOException {
      BufferedInputStream bis = new BufferedInputStream(inputStream);
      FileOutputStream fis = new FileOutputStream(path);
      int readBytes = 0;
      int totalBytes = 0;
      byte[] buffer = new byte[10000];
      while ((readBytes = bis.read(buffer, 0, 10000)) != -1) {
         fis.write(buffer, 0, readBytes);
         totalBytes += readBytes;
      }
      bis.close();
      fis.close();
      return totalBytes;
   }

   public static void deleteFromDisk(String path) {
      new File(path).delete();
   }

   public static String readFile(String string) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtils.class.getClassLoader().getResourceAsStream(string)));
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
         buffer.append(line);
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
