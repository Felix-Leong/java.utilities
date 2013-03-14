package de.ebf.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

public class FormatUtils {
	
	public static final SimpleDateFormat DATE_FORMAT_METRIC = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATETIME_FORMAT_METRIC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final String DEFAULT_COUNTRY_CODE = "49";
	
	private static final Logger log = Logger.getLogger(FormatUtils.class);
	
	public static String readableFileSize(Long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	public static String toMSISDN(String phonenumber, String masterSIM){
		String msisdn=phonenumber;
		try{
			if(msisdn.substring(0,1).equals("+")){
				msisdn=msisdn.substring(1);
			}else if(msisdn.substring(0,2).equals("00")){
				msisdn=msisdn.substring(2);
			}else if(msisdn.substring(0,1).equals("0")){
				msisdn=DEFAULT_COUNTRY_CODE+msisdn.substring(1);
			}else if(msisdn.substring(0,2).equals("17") || msisdn.substring(0,2).equals("16") || msisdn.substring(0,2).equals("15")){
				msisdn=DEFAULT_COUNTRY_CODE+msisdn;
			}else{
				try{
					Long.parseLong(msisdn);
				}catch(Exception ex){
					msisdn=masterSIM;
				}			
			}			
		}catch(Exception ex){
			log.error(ex);
		}
		log.debug("toMSISDN: "+phonenumber+" -> "+msisdn);
		return msisdn;
	}

}
