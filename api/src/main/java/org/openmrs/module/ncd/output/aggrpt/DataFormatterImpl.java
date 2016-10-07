package org.openmrs.module.ncd.output.aggrpt;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.NumberUtilities;
import org.openmrs.module.ncd.utilities.StringUtilities;

public class DataFormatterImpl implements DataFormatter {
    
    public static final String PROP_TEMPLATE = "DataFormatterImpl.template";
    public static final String PROP_TEMPLATE_PATHNAME = "DataFormatterImpl.templatePathname";
    
    private static Log logger = LogFactory.getLog(DataFormatterImpl.class);
    private Map<String, String> properties;
    private String template = null;
    
    public void configure(Map<String, String> properties, String resourceName) {
        
        this.properties = properties;

        if (properties.containsKey(PROP_TEMPLATE)) {

            template = properties.get(PROP_TEMPLATE);
        }
        else if (properties.containsKey(PROP_TEMPLATE_PATHNAME)) {
            
            // Get the report template which is stored as a file in the local file system
            template = loadFile(properties.get(PROP_TEMPLATE_PATHNAME));
        }
        else {
            
            template = loadResource(resourceName);
        }
    }
    
    private String loadFile(String pathname) {
        
        try {
            return loadStream(new FileInputStream(pathname));
        }
        catch (Exception e) {
            logger.error("error reading template from file (" + pathname + "): " + e.getMessage(), e);
            return "Error loading template from pathname \"" + pathname + "\".";
        }
    }
    
    private String loadResource(String resourceName) {
        
        try {
            return loadStream(this.getClass().getClassLoader().getResourceAsStream(resourceName));
        }
        catch (Exception e) {
            
            logger.error("error reading template from resource (" + resourceName + "): " + e.getMessage(), e);
            return "Error loading template from resource \"" + resourceName + "\".";
        }
    }
    
    private String loadStream(InputStream rawIn) throws IOException {
        
        BufferedInputStream in = new BufferedInputStream(rawIn);
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = in.read()) != -1) {
            sb.append((char) ch);
        }
        in.close();
        
        return sb.toString();
    }
    
    public String format(Object data) {

        try
        {
            // Initialize velocity (singleton)
            Velocity.init();
            
            // Create a context and load the report data into it
            VelocityContext context = new VelocityContext();
            context.put("data", data);

            // Put formatting helper classes into the context
            context.put("dateutil", new DateUtilities());
            context.put("numberutil", new NumberUtilities());
            context.put("stringutil", new StringUtilities());

            // Put the recipient display name into the context
            context.put("RecipientDisplayName", properties.get(DataFormatterFactory.PROP_RECIPIENT));
            
            // Evaluate the template
            StringWriter sw = new StringWriter();
            Velocity.evaluate(context, sw, "aggrpt", template);
            
            return sw.toString();
        }
        catch (IOException ioe)
        {
            logger.error("Unexpected exception: " + ioe.getMessage(), ioe);
            // something invoked in the template threw an exception
            return "Velocity error: " + ioe;
        }
        catch (Exception e)
        {
            // possible init() exception
            logger.error("Unexpected exception: " + e.getMessage(), e);
            return "Velocity error: " + e;
        }
    }
}
