/**
 * Copyright 2007 Regenstrief Institute, Inc. All Rights Reserved.
 */
package org.openmrs.module.ncd.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.database.CodeFrequency;
import org.openmrs.module.ncd.storage.CodeFrequencyStorageException;
import org.openmrs.util.OpenmrsUtil;

/**
 * This map is used to cache loinc code frequency data
 * before it is persisted to the database.
 * 
 * @author jlbrown
 *
 */
public class CodeFrequencyCache
{
    private static Log logger = LogFactory.getLog(CodeFrequencyCache.class);    
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
            "yyyyMMdd");
    private static final String CACHE_FILE_NAME = "code_frequency_cache.txt";
    
    private HashMap<String, CodeFrequency> codeFreqMap;
    private FileWriter cacheWriter = null;

    public CodeFrequencyCache() throws CodeFrequencyStorageException
    {
        codeFreqMap = new HashMap<String, CodeFrequency>();         
        try
        {
            String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
            logger.debug("Code Frequency Cache File is " + appDataDir + CACHE_FILE_NAME);
            File loincFreqFile = new File(appDataDir + CACHE_FILE_NAME);            
            if (loincFreqFile.exists())
            {
                doRecovery(loincFreqFile);
            }
            cacheWriter = new FileWriter(loincFreqFile, true);
        }
        catch (Exception e)
        {
        	logger.error("recovery failed: " + e.getMessage(), e);
            throw new CodeFrequencyStorageException(e);
        }
    }
    
    public Collection<CodeFrequency> getMapValues()
    {
        return codeFreqMap.values();
    }
    
    public int getSize()
    {
        return codeFreqMap.size();
    }

    public void incrementCodeFrequency(String code, String codeSystem,
            String application, String facility, String location, Date date, 
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode) 
    throws CodeFrequencyStorageException
    {
        String keyString = getKeyString(code, codeSystem, application, facility,
        		location, date, patientZipCode, instituteZipCode, doctorZipCode);
        CodeFrequency item = codeFreqMap.get(keyString);
        if (item == null)
        {
            item = new CodeFrequency(application, facility, location, code, codeSystem, patientZipCode, instituteZipCode, doctorZipCode, date, 0);            
        }
        item.setCount(item.getCount() + 1);
        codeFreqMap.put(keyString, item);
        if (cacheWriter != null)
        {
            writeToCache(keyString);            
        }
    }
    
    private void writeToCache(String keyString) throws CodeFrequencyStorageException {
    	try {
    		cacheWriter.write(keyString);
            cacheWriter.write("\n");
            cacheWriter.flush();
    	} catch (IOException e) {
            // try to recover from an IOException
            try {
                String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
                cacheWriter = new FileWriter(appDataDir + CACHE_FILE_NAME, true);
                writeToCache(keyString);
            } catch (Exception e2) {
            	logger.error("write to cache failed: " + e2.getMessage(), e2);
                tryToCloseCacheFile();
                throw new CodeFrequencyStorageException(e2);
            }
        } catch (Exception e) {
        	logger.error("write to cache failed: " + e.getMessage(), e);
            tryToCloseCacheFile();
            throw new CodeFrequencyStorageException(e);
        }
        
    }

    private void tryToCloseCacheFile() {
        try
        {
            cacheWriter.close();
        }
        catch (Exception e2)
        {
            // do nothing;
        }
    }    
    
    public CodeFrequency findCodeFrequency(String code, String codeSystem, 
            String application, String facility, String location, Date date,
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode)
    {
        String keyString = getKeyString(code, codeSystem, application, facility,
                location, date, patientZipCode, instituteZipCode, doctorZipCode);
        return codeFreqMap.get(keyString);
    }

    public void addItem(CodeFrequency item)
    {
        String keyString = getKeyString(item.getCode(), item.getCodeSystem(),
                item.getApplication(), item.getFacility(), item.getLocation(),
                item.getDate(), item.getPatientZipCode(),
                item.getInstituteZipCode(), item.getDoctorZipCode());
        codeFreqMap.put(keyString, item);
    }

    public void addCollection(Collection<CodeFrequency> col)
    {
        for( CodeFrequency item : col )
        {
            addItem(item);
        }
    }
    
    public void clearMap() throws CodeFrequencyStorageException
    {
        codeFreqMap.clear();
        
        try
        {
            cacheWriter.close();
            String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
            cacheWriter = new FileWriter(appDataDir + CACHE_FILE_NAME, false);
        }
        catch (Exception e)
        {
            throw new CodeFrequencyStorageException(e);
        }
    }
    
    private String getKeyString(String code, String codeSystem, 
            String application, String facility, String location, Date date, 
            String patientZipCode, String instituteZipCode, 
            String doctorZipCode )
    {
        StringBuilder sb = new StringBuilder();
        sb.append(code);
        sb.append(":");
        sb.append(codeSystem);
        sb.append(":");
        sb.append(application);
        sb.append(":");
        sb.append(facility);
        sb.append(":");
        sb.append(location);
        sb.append(":");
        sb.append(getDateString(date));
        sb.append(":");
        sb.append(patientZipCode);
        sb.append(":");
        sb.append(instituteZipCode);
        sb.append(":");
        sb.append(doctorZipCode);
        return sb.toString();
    }

    private void doRecovery(File lfFile) throws CodeFrequencyStorageException
    {
        long count = 0;
        long time = System.currentTimeMillis(); 
        logger.info("begin recovery.");
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(lfFile));
            String line = reader.readLine();
            while (line != null)
            {
                String[] keyStringElements = line.split(":", 9);
                incrementCodeFrequency(keyStringElements[0], 
                        keyStringElements[1], keyStringElements[2], 
                        keyStringElements[3], keyStringElements[4],
                        getDateObject(keyStringElements[5]),
                        keyStringElements[6], keyStringElements[7], 
                        keyStringElements[8]);
                count++;
                line = reader.readLine();
            }
        }
        catch (Exception e)
        {
            throw new CodeFrequencyStorageException(e);
        }
        finally {
            try {
                reader.close();
            } catch (IOException e) {
                // if we can't close, do nothing
            }
            time = System.currentTimeMillis() - time;
            logger.info("end recovery of " + count + " items in " + time + "ms.");
        }
    }

    private String getDateString(Date date)
    {
        return dateFormatter.format(date);
    }
    
    private Date getDateObject(String date)
    {
        Date retVal = null;
        try {
            retVal = dateFormatter.parse(date);            
        } catch (ParseException e) {
            logger.debug(e.getMessage());
        }        
        return retVal;
    }
}
