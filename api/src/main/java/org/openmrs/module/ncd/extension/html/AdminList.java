/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ncd.extension.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.ncd.utilities.NCDConstants;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * This class defines the links that will appear on the administration page
 * under the "ncd.title" heading. This extension is enabled by defining
 * (uncommenting) it in the /metadata/config.xml file.
 */
public class AdminList extends AdministrationSectionExt
{
    public Extension.MEDIA_TYPE getMediaType()
    {
        return Extension.MEDIA_TYPE.html;
    }

    public String getTitle()
    {
        return "ncd.title";
    }

    public Map<String, String> getLinks()
    {
        Map<String, String> map = new LinkedHashMap<String, String>();

        map.put("module/ncd/dashboard.form", "ncd.pages.dashboard.linkname");  // Dupe

        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_DASHBOARD))
        {
            map.put("module/ncd/dashboard.form", "ncd.pages.dashboard.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_ALERTS))
        {
            map.put("module/ncd/alertList.form", "ncd.pages.alertList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_CODES))
        {
            map.put("module/ncd/codeList.form", "ncd.pages.codeList.linkname");
        }

        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_CODES))
        {
            map.put("module/ncd/codeSystemList.form", "ncd.pages.codeSystemList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_CODES))
        {
            map.put("module/ncd/codeTypeList.form", "ncd.pages.codeTypeList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_CONDITIONS))
        {
            map.put("module/ncd/conditionList.form", "ncd.pages.conditionlist.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_DECIDED_RESULTS))
        {
            map.put("module/ncd/decidedResultList.form", "ncd.pages.decidedresultlist.linkname");
        }
        
        // TODO: this needs a new privilege: PRIV_VIEW_ERRORS
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_DASHBOARD))
        {
            map.put("module/ncd/errorList.form", "ncd.pages.errorList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_MESSAGE_SOURCES))
        {
            map.put("module/ncd/producerList.form", "ncd.pages.producerList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_INSTITUTIONS))
        {
            map.put("module/ncd/institutionList.form", "ncd.pages.institutionList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_CRITICS))
        {
            map.put("module/ncd/criticList.form", "ncd.pages.criticlist.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_NLP_CONTEXTS))
        {
            map.put("module/ncd/contextTypeList.form", "ncd.pages.contextTypeList.linkname");
        }
        
        // TODO: this needs a new privilege: PRIV_VIEW_NLP_DISCRETE_TERMS
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_DASHBOARD))
        {
            map.put("module/ncd/discreteTermList.form", "ncd.pages.discreteTermList.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_REPORTABLE_RESULTS))
        {
            map.put("module/ncd/reportableResultList.form", "ncd.pages.reportableresultlist.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_SCHEDULED_REPORTS))
        {
            map.put("module/ncd/reportList.form", "ncd.pages.reportlist.linkname");
        }
        
        if (Context.hasPrivilege(NCDConstants.PRIV_VIEW_NOTIFIER_CRITICS))
        {
            map.put("module/ncd/notifierCritic.form", "ncd.pages.notifierCritic.linkname");
        }

        //map.put("module/ncd/ncdLink.form", "Links");

        return map;
    }

}
