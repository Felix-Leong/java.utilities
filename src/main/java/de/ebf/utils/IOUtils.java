package de.ebf.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

	public static void saveToDisk(InputStream inputStream, String path) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		FileOutputStream fis = new FileOutputStream(path);
		int readBytes = 0;
		byte[] buffer = new byte[10000];
		while ((readBytes = bis.read(buffer, 0, 10000)) != -1) {
			fis.write(buffer, 0, readBytes);
		}
		bis.close();
		fis.close();
	}

	public static void deleteFromDisk(String path) {
		new File(path).delete();
	}

}
