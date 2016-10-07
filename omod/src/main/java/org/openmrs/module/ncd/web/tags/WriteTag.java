package org.openmrs.module.ncd.web.tags;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ncd.utilities.DateUtilities;
import org.openmrs.module.ncd.utilities.HL7Utilities;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.NestedPathTag;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

@SuppressWarnings("deprecation")
public class WriteTag extends TagSupport 
{
    private static final long serialVersionUID  = 7241048195826215674L;
    private final Log log                       = LogFactory.getLog(getClass());

    private String path;
    private Object value;
    private String type;
    private String format;

    public int doStartTag() throws JspTagException 
    {    
        if (path == null && value == null) 
        {    
            throw new JspTagException("Either the path or value attribute must be specified.");
        }

        String output = "";

        if (type != null) 
        {    
            if ("hl7".equals(type)) 
            {    
                output = formatHL7(getSourceValue(null).toString());
            }
            else 
            {
                DateFormat fmt  = null;
                Object v        = getSourceValue(java.util.Date.class);
            
                if ("date".equals(type)) 
                {    
                    fmt = DateUtilities.getDateFormat();
                }
                else if ("datetime".equals(type)) 
                {    
                    fmt = DateUtilities.getDateTimeFormat();
                }
                else if ("time".equals(type)) 
                {    
                    fmt = DateUtilities.getTimeFormat();
                }
                else 
                {    
                    throw new JspTagException("Type value of type must be date, datetime or time.");
                }
    
                if (v == null) 
                {
                	output = "";
                }
                else 
                {
                	output = fmt.format((Date) v);
                }
            }
        }
        else if (format != null) 
        {    
            DateFormat fmt  = new SimpleDateFormat(format);
            Object v        = getSourceValue(java.util.Date.class);
            
            if (v == null) 
            {
            	output = "";
            }
            else 
            {
            	output = fmt.format((Date) v);
            }
        }
        else 
        {
            Object v = getSourceValue(null);
            
            if (v == null) 
            {
            	output = "";
            }
            else 
            {
            	output = v.toString();
            }
        }
        
        try 
        {
            pageContext.getOut().write(output);
        }
        catch (IOException e) 
        {
            log.error(e);
        }

        release();

        return SKIP_BODY;
    }
    
    /**
     * Clean up the variables
     */
    public void release() 
    {
        super.release();
        
        this.path   = null;
        this.type   = null;
        this.format = null;
        this.value  = null;
    }
    
    private Object getSourceValue(Class<?> clazz) throws JspTagException 
    {    
        if (value != null) 
        {    
            return value;
        }
        else 
        {
            RequestContext requestContext = (RequestContext) this.pageContext.getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
    
            try 
            {    
                // get the "path" object from the pageContext
                String resolvedPath     = ExpressionEvaluationUtils.evaluateString("path", this.path, pageContext);
                String nestedPath       = (String) pageContext.getAttribute( NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
                
                if (nestedPath != null) 
                {
                    resolvedPath = nestedPath + resolvedPath;
                }
        
                BindStatus status = new BindStatus(requestContext, resolvedPath, false);
                log.debug("status: " + status);

                Object pathValue = status.getValue();
                log.debug("status.value: " + pathValue);
                
                if (pathValue != null) 
                {
                    
                    if (clazz != null) 
                    {    
                        if (!instanceOf(pathValue.getClass(), clazz)) 
                        {    
                            // Maybe the spring "editor" got in the way.
                            log.debug("status.valueType: " + status.getValueType());
                         
                            return status.getEditor().getValue();
                        }
                    }

                    return pathValue;
                }
                else 
                {
                    return null;
                }
            }
            catch (JspException je) 
            {     
                throw new JspTagException(je);
            }
        }
    }

    /**
     * Tests if instanceClass equals, extends or implements 
     * requiredClass, recursively.
     * 
     * @param instanceClass
     * @param requiredClass
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    private boolean instanceOf(Class instanceClass, Class requiredClass) 
    {    
        // If the instance is an instance of the class, true
        if (instanceClass.equals(requiredClass)) 
        {
            return true;
        }

        // If the instance extends the required class, true
        Class instanceClassSuperclass = instanceClass.getSuperclass();
        
        if (instanceClassSuperclass != null && instanceOf(instanceClassSuperclass, requiredClass)) 
        {
            return true;
        }
        
        // If the class implements the required class, true
        Class[] instanceClassInterfaces = instanceClass.getInterfaces();
        
        for (Class interfaceClass : instanceClassInterfaces) 
        {
            if (instanceOf(interfaceClass, requiredClass)) 
            {
                return true;
            }
        }

        // Otherwise, false.
        return false;
    }

    private String formatHL7(String msg) 
    {
        // TODO: add an attribute for the max segment width.

        return HL7Utilities.toHTML(msg, 80);
    }

    /**
     * @return the path
     */
    public String getPath() 
    {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) 
    {
        this.path = path;
    }

    /**
     * @return the type
     */
    public String getType() 
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) 
    {
        this.type = type;
    }

    /**
     * @return the format
     */
    public String getFormat() 
    {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) 
    {
        this.format = format;
    }

    /**
     * @return the value
     */
    public Object getValue() 
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) 
    {
        this.value = value;
    }
}
