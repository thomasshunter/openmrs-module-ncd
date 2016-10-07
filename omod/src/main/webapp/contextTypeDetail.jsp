<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="ncd" uri="resources/ncd.tld" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.openmrs.module.ncd.utilities.DateUtilities" %>
<%@ page import="org.openmrs.module.ncd.database.DecidedResult" %>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/ncd/ncd.css" />

<openmrs:require privilege="View NCD NLP Contexts" otherwise="/login.htm" />

<h2><spring:message code="ncd.pages.contextTypeDetail.title" /></h2>

<br/>

<c:set var="isNew" value="${getNcdContextTypeDetailObj.id == null}" />

<b class="boxHeader"><spring:message code="ncd.pages.contextTypeDetail.title"/></b>
<form:form cssClass="box" commandName="getNcdContextTypeDetailObj">
	<table>
		<thead>
			<tr>
				<td colspan="2">
					<openmrs:hasPrivilege privilege="Add NCD NCD NLP Contexts,Edit NCD NCD NLP Contexts">
						<input type="submit" value="<spring:message code="ncd.buttons.save"/>" name="action">
					</openmrs:hasPrivilege>
					<input type="submit" value="<spring:message code="ncd.buttons.cancel"/>" name="action">
				</td>
			</tr>
			<tr>
				<td colspan="2">
					&nbsp;
				</td>
			</tr>
		</thead>
	    <tbody>
			<c:if test="${!isNew}">
		    	<tr>
		    		<td>
		    			<spring:message code="ncd.pages.contextTypeDetail.id"/>
		    		</td>
		    		<td>
		    			<ncd:write path="id" />
		    		</td>
		    	</tr>
	    	</c:if>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.typeName"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
					<c:if test="${isNew}">
		    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.typeName.title"/></c:set>
		    			<form:input path="typeName" title="${title}"/>
						<form:errors path="typeName" cssClass="error" />
    				</c:if>
					<c:if test="${!isNew}">
	    				<ncd:write path="typeName" />
    				</c:if>
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.description"/>
	    			<spring:message code="ncd.required"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.description.title"/></c:set>
	    			<form:textarea path="description" rows="5" cols="80" title="${title}" />
					<form:errors path="description" cssClass="error" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.usesContextGroup"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.usesContextGroup.title"/></c:set>
	    			<form:checkbox path="usingContextGroup" title="${title}" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.usesPreString"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.usesPreString.title"/></c:set>
	    			<form:checkbox path="usingPreString" title="${title}" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.usesPostString"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.usesPostString.title"/></c:set>
	    			<form:checkbox path="usingPostString" title="${title}" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.matchNegation"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.matchNegation.title"/></c:set>
	    			<form:checkbox path="matchNegation" title="${title}" />
	    		</td>
	    	</tr>
	    	<tr>
	    		<td>
	    			<spring:message code="ncd.pages.contextTypeDetail.usesSmallWindow"/>
	    		</td>
	    		<td>
	    			<c:set var="title"><spring:message code="ncd.pages.contextTypeDetail.usesSmallWindow.title"/></c:set>
	    			<form:checkbox path="usingSmallWindow" title="${title}" />
	    		</td>
	    	</tr>
		</tbody>
	</table>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
