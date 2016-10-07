package org.openmrs.module.ncd.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SortColumnTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(getClass());

    private String sortkey;
    private String title;
    private String cssclass;
    
	public int doStartTag() throws JspTagException {
        
        if (sortkey == null) {
            
            throw new JspTagException("The sortkey attribute must be specified with a value.");
        }

        String output = "";
        
        SortColumnsTag parent = (SortColumnsTag) getParent();

        output = "<span";
        
        if (cssclass != null) {
        	output += " class=\"" + cssclass + "\"";
        }
        
        if (title != null) {
        	output += " title=\"" + title + "\"";
        }
        
        output += " onclick=\"document." + parent.getFormname() + ".sortaction.value='" + sortkey + "';document." + parent.getFormname() + ".submit();\">";
        
        try {
            pageContext.getOut().write(output);
        }
        catch (IOException e) {
            log.error(e);
        }

        return EVAL_BODY_INCLUDE;
    }

	public int doEndTag() {

        String output = "";
        
        SortColumnsTag parent = (SortColumnsTag) getParent();

        if (sortkey.equals(parent.getSortkey())) {
        	
        	// If direction==true (i.e. isSortAscending()==true) (it's ascending, "larger" values at the bottom of the list)
        	String icon = "down.png";
            if ("true".equals(parent.getDirection())) {
            	icon = "up.png";
            }
           	output += "<img src=\"" + parent.getResourcepath();
           	if (output.charAt(output.length()- 1) != '/') {
           		output += "/";
           	}
           	output += icon + "\" border=\"0\">";
        }
        
        output += "</span>";

        try {
            pageContext.getOut().write(output);
        }
        catch (IOException e) {
            log.error(e);
        }

        release();

		return EVAL_PAGE;
	}
	
    /**
     * Clean up the variables
     */
    public void release() {
        super.release();
        this.sortkey = null;
        this.title = null;
        this.cssclass = null;
    }

	public String getSortkey() {
		return sortkey;
	}

	public void setSortkey(String sortkey) {
		this.sortkey = sortkey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCssclass() {
		return cssclass;
	}

	public void setCssclass(String cssclass) {
		this.cssclass = cssclass;
	}
}
