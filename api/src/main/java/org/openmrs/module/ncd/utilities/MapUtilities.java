package org.openmrs.module.ncd.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapUtilities {

    public static String get(Map<String, String> properties, String key, String defValue) {
        
        String v = properties.get(key);
        if (v != null) {
            return v;
        }
        else {
            return defValue;
        }
    }

    public static int get(Map<String, String> properties, String key, int defValue) {
        
        String v = properties.get(key);
        if (v != null) {
            int ivalue = Integer.parseInt(v);
            return ivalue;
        }
        else {
            return defValue;
        }
    }

    public static long get(Map<String, String> properties, String key, long defValue) {
        
        String v = properties.get(key);
        if (v != null) {
            long ivalue = Long.parseLong(v);
            return ivalue;
        }
        else {
            return defValue;
        }
    }

    public static double get(Map<String, String> properties, String key, double defValue) {
        
        String v = properties.get(key);
        if (v != null) {
            return Double.parseDouble(v);
        }
        else {
            return defValue;
        }
    }

    public static boolean get(Map<String, String> properties, String key, boolean defValue) {
        
        String v = properties.get(key);
        if (v != null) {
            boolean value = Boolean.parseBoolean(v);
            return value;
        }
        else {
            return defValue;
        }
    }

    public static Date get(Map<String, String> properties, String keyDate, String keyTime, String defTime, Date defValue) {
    	
    	try {
	    	String vDate = properties.get(keyDate);
	    	String vTime = properties.get(keyTime);
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	    	dateFormat.setLenient(false);
	    	if (vDate != null && vTime != null) {
		    	return dateFormat.parse(vDate + " " + vTime);
	    	}
	    	else if (vDate != null) {
		    	return dateFormat.parse(vDate + " " + defTime);
	    	}
	    	else {
	    		return defValue;
	    	}
    	} catch (ParseException pe) {
    		// This should be a can't happen due to the validation, but if it does, just return the default date
    		return defValue;
    	}
    }

    public static Date get(Map<String, String> properties, String key, Date defValue) {
        
        String vDate = properties.get(key);
        if (vDate != null) {
            return DateUtilities.parseDateTime(vDate);
        }
        else {
            return defValue;
        }
    }
    
    public static String[] get(Map<String, String> properties, String key, String[] defValue) {
        
        String v = properties.get(key);
        if (v != null) {
            return StringUtilities.fromCSV(v);
        }
        else {
            return defValue;
        }
    }

    public static String[] getCSV(Map<String, String> properties, String key) {
        
        String v = StringUtilities.trim(properties.get(key));
        if (v != null) {
            return StringUtilities.fromCSV(v);
        }
        else {
            return null;
        }
    }
    
    public static Set<String> getCSVStringSet(Map<String, String> properties, String key) {
        
        String[] value = getCSV(properties, key);
        if (value == null) {
            return null;
        }
        
        Set<String> result = new HashSet<String>(value.length);
        result.addAll(Arrays.asList(value));
        return result;
    }
}
