<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="org.openmrs.module.ncd.utilities.DateUtilities" %>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<h2>Debug Information</h2>

<p>This page displays information useful in debugging certain problems
with the NCD and OpenMRS. The information displayed varies depending
on the recent needs of the developers.</p>

<br/>

<form:form commandName="getNcdDebugObj">

	<%-- Alert list section --%>
	<b class="boxHeader"><spring:message code="ncd.pages.alertlist.results.title"/></b>
	<div class="box">
		<div class="scrollable" style="height: 380px">		
			<table cellspacing="0" class="scrollable list">
			    <tbody style="height: 350px">
			    	<tr style="height: 12px">
						<td>
							Default locale:
						</td>
						<td>
							<%= Locale.getDefault() %>
						</td>
			    	</tr>
			    	<tr style="height: 12px">
						<td>
							NCD standard date format:
						</td>
						<td>
							<%= ((SimpleDateFormat) DateUtilities.getDateFormat()).toPattern() %>
						</td>
			    	</tr>
			    	<tr style="height: 12px">
						<td>
							NCD standard time format:
						</td>
						<td>
							<%= ((SimpleDateFormat) DateUtilities.getTimeFormat()).toPattern() %>
						</td>
			    	</tr>
			    	<tr style="height: 12px">
						<td>
							NCD standard date+time format:
						</td>
						<td>
							<%= ((SimpleDateFormat) DateUtilities.getDateTimeFormat()).toPattern() %>
						</td>
			    	</tr>
				</tbody>
			</table>
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
