package org.openmrs.module.ncd.web.tags;

import java.io.IOException;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SortColumnsTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(getClass());

	private String formname;
	private String url;
    private String sortkey;
    private String direction;
    private String resourcepath;
    
    public int doStartTag() {
        try {
            pageContext.getOut().write("<input type=\"hidden\" name=\"sortaction\" value=\"\"/>");
        }
        catch (IOException e) {
            log.error(e);
        }

    	return EVAL_BODY_INCLUDE;
    }
    
	public int doEndTag() {
		release();
		return EVAL_PAGE;
	}
	
    /**
     * Clean up the variables
     */
    public void release() {
        super.release();
        this.url = null;
        this.sortkey = null;
        this.direction = null;
        this.resourcepath = null;
    }

    public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSortkey() {
		return sortkey;
	}
	public void setSortkey(String sortkey) {
		this.sortkey = sortkey;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getResourcepath() {
		return resourcepath;
	}
	public void setResourcepath(String resourcepath) {
		this.resourcepath = resourcepath;
	}

	public String getFormname() {
		return formname;
	}

	public void setFormname(String formname) {
		this.formname = formname;
	}
}
