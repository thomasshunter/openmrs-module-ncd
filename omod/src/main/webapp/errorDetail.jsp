<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />
<openmrs:htmlInclude file="/moduleResources/ncd/ncd.js" />
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-std.css" />

<!--[if IE]>
<openmrs:htmlInclude file="/moduleResources/ncd/scrollable-table-ie.css" />
<![endif]-->

<openmrs:require privilege="View NCD Errors" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.errordetail.linkname" /></h2>

<script type="text/javascript">
</script>

<form:form commandName="getNcdErrorDetailObj">
	<br/>
	<div class="scrollable" style="height:600px">
		<b class="boxHeader"><spring:message code="ncd.pages.errordetail.section.error"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td width="1">
		    			<spring:message code="ncd.pages.errordetail.id"/>
		    		</td>
		    		<td width="*">
						<c:set var="title"><spring:message code="ncd.pages.errordetail.id.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="error.id" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.errordetail.lasterrordate"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.errordetail.lasterrordate.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="error.lastErrorDate" type="datetime"/>
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.errordetail.level"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.errordetail.level.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="error.level" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.errordetail.description"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.errordetail.description.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="error.description" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.errordetail.additionalinfo"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.errordetail.additionalinfo.title"/></c:set>
			        	<a title="${title}" class="unmarked">
							<c:choose>
								<c:when test="${getNcdErrorDetailObj.error.additionalInfo != null}">
		    						<ncd:write path="error.webFormattedAdditionalInfo" />
								</c:when>
								<c:otherwise>
									Not available
								</c:otherwise>
							</c:choose>
		    			</a>
		    		</td>
		    	</tr>
 			</tbody>
		</table>
		<br/>
		<b class="boxHeader"><spring:message code="ncd.pages.errordetail.section.message"/></b>
		<table class="box">
		    <tbody>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.errordetail.mpqseqnumber"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.errordetail.mpqseqnumber.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="error.mpqSeqNumber" />
		    			</a>
		    		</td>
		    	</tr>
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.errordetail.tblrawhl7"/>
		    		</td>
		    		<td>
						<c:set var="title"><spring:message code="ncd.pages.errordetail.tblrawhl7.title"/></c:set>
			        	<a title="${title}" class="unmarked">
			    			<ncd:write path="error.rawMessage.messageText" type="hl7" />
		    			</a>
		    		</td>
		    	</tr>
			</tbody>
		</table>
		<br/>
	</div>
	<hr/>
	<input type="submit" value="<spring:message code="ncd.buttons.close"/>" name="action" />
	<openmrs:hasPrivilege privilege="Edit NCD Errors">
		<input type="submit" value="<spring:message code="ncd.pages.errordetail.buttons.reprocess"/>" name="action">
	</openmrs:hasPrivilege>
</form:form>
 
<%@ include file="/WEB-INF/template/footer.jsp"%>
