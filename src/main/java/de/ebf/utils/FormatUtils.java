package de.ebf.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class FormatUtils {
	
	public static final SimpleDateFormat DATE_FORMAT_METRIC = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATETIME_FORMAT_METRIC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String readableFileSize(Long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}
